# PEAP — Public Experience Analytics Platform (micro-lab)

A learning lab implementing the MVP of the **Public Experience Analytics
Platform** (see [Product Analysis & Initiation Document (AID).pdf](<documets/Product Analysis & Initiation Document (AID).pdf>)):
a crowd-driven platform where users register, rate, and review public
entities (products, restaurants, services), with real-time analytics and
stream-based fraud detection.

Built end-to-end as a hands-on exercise: local containers → local
Kubernetes → Google Cloud, with CI/CD from GitHub.

## Architecture

```
                        ┌──────────────────────────────────────────────┐
 client ──► api-gateway ├─► identity-service ──┐                       │
   ▲   (blocklist check │   product-service ───┤                       │
   │    + api-request   │   voting-service ────┼─► Kafka topics        │
   403    events)       │   review-service ────┘   user-created        │
   │                    │                          entity-created      │
   │                    │                          vote-submitted      │
   │                    │                          review-submitted    │
   │                    │                          api-request         │
   │                    └──────────────────────────────┬───────────────┘
   │                                                   │
   │                              ┌────────────────────┴─────────────┐
   │                              ▼                                  ▼
   │                     analytics-service                 Flink fraud job
   │                     (counts, leaderboards,            (sliding-window
   │                      endpoint traffic stats)           request-flood rule)
   │                                                              │
   └───────────────── Redis blocklist (TTL) ◄─────────────────────┤
                                                                  ▼
                                                        fraud-detected topic
```

- **Synchronous path**: client → gateway → service → Postgres (one DB per service)
- **Asynchronous path**: services publish domain events to Kafka; consumers
  (analytics, Flink) each read with their own consumer group
- **Fraud loop**: gateway publishes `api-request` events → Flink counts per
  client IP over a sliding window → breaching IPs written to Redis with a
  TTL → gateway rejects them with 403 until the block expires

## Modules (`services/`)

| Module | Kind | Purpose |
|---|---|---|
| `api-gateway` | Spring Cloud Gateway | Routing, blocklist enforcement, api-request event publishing |
| `identity-service` | Spring Boot | Registration, login, JWT issuance |
| `product-service` | Spring Boot | Public entities (create/list/get) |
| `voting-service` | Spring Boot | Votes, Redis-cached score aggregation |
| `review-service` | Spring Boot | Reviews, moderation flagging |
| `analytics-service` | Spring Boot (Kafka consumer) | Platform counters, entity stats, leaderboard, endpoint traffic stats |
| `fraud-detection-job` | Apache Flink | Request-flood detection, Redis blocklist writer |

Java 17, Spring Boot 3.3, Flink 1.20, multi-module Maven
([services/README.md](services/README.md) has service-level detail).

## Run locally (Docker Compose)

Requires a Docker daemon (this project uses colima on macOS) and Maven.

```bash
colima start --cpu 4 --memory 6          # or Docker Desktop

cd services && mvn -B clean package -DskipTests && cd ..
docker-compose build
docker-compose up -d
```

| Endpoint | URL |
|---|---|
| API gateway | http://localhost:8080 |
| Kafka UI | http://localhost:8090 |
| Flink UI | http://localhost:8091 |
| Postgres | localhost:5432 (`appuser` / `changeme`) |

Try it:

```bash
curl -X POST localhost:8080/api/v1/users/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.com","password":"password123"}'

curl localhost:8080/api/v1/analytics/summary
```

Fraud demo — flood the gateway past the local threshold (20 req / 60s
window), get blocked, wait out the 120s TTL:

```bash
for i in $(seq 1 35); do curl -s -o /dev/null localhost:8080/api/v1/entities; done
sleep 15
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/v1/entities   # 403
```

## Run on Kubernetes

Kustomize manifests live in [`k8s/`](k8s): a shared `base/` plus overlays.

| Overlay | Namespace | Target | Images |
|---|---|---|---|
| `local` | `peap-local` | kind cluster | `peap/<svc>:local` (kind-loaded) |
| `qa` | `peap-qa` | GKE | Artifact Registry, LoadBalancer gateway, persistent Postgres disk |
| `prod` | `peap-prod` | (aspirational) | 3 replicas + HPAs |

**Local (kind):**

```bash
kind create cluster --name micro-lab
# build images tagged :local, then load each into the cluster:
kind load docker-image peap/<service>:local --name micro-lab
kubectl apply -k k8s/overlays/local
kubectl -n peap-local port-forward svc/api-gateway 8080:80
```

**GKE (qa):** create the cluster, then deploy via the pipeline (below).

```bash
gcloud container clusters create-auto peap --region us-central1
# ... run the Deploy workflow ...
gcloud container clusters delete peap --region us-central1 --quiet   # when done (~$5-8/day while up)
```

QA Postgres data survives cluster delete/create cycles: it binds a static
PersistentVolume on a pre-created disk (`peap-qa-postgres`,
us-central1-a). **Do not delete that disk during teardown.**

## CI/CD (GitHub Actions, manual triggers only)

Both workflows authenticate to GCP via Workload Identity Federation — no
stored keys.

- **Build and push images** — builds all jars, builds amd64 images for all
  7 modules, pushes to Artifact Registry tagged `latest` + short commit SHA
- **Deploy to GKE (qa)** — input: image tag (default `latest`); checks the
  cluster exists, retags the qa overlay via `kustomize edit set image`,
  applies, waits for all rollouts, prints the gateway's public IP

Run both from the GitHub **Actions** tab.

## API quick reference (via gateway)

```
POST /api/v1/users/register            {email, password}
POST /api/v1/auth/login                {email, password} -> JWT
GET  /api/v1/users/{id}
POST /api/v1/entities                  {name, category, description?}
GET  /api/v1/entities[?category=]
GET  /api/v1/entities/{id}
POST /api/v1/votes                     {entityId, userId, value 1-5}
GET  /api/v1/votes/entity/{id}/score
POST /api/v1/reviews                   {entityId, userId, text}
GET  /api/v1/reviews/entity/{id}
POST /api/v1/reviews/{id}/flag
GET  /api/v1/analytics/summary
GET  /api/v1/analytics/entities/{id}
GET  /api/v1/analytics/leaderboard[?category=&limit=]
GET  /api/v1/analytics/requests[?limit=]
```

Note: endpoints are currently unauthenticated — JWTs are issued but not
yet enforced at the gateway. That's the next security milestone.

## Hard-won operational notes

- **Single-broker Kafka needs** `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1`
  or consumer groups silently never form (internal topic defaults to RF 3).
- **Kafka on kind** needs the controller quorum voter at `localhost:9093`,
  not the Service name — kind's CNI can't hairpin a pod to itself.
- **Reused image tags need** `imagePullPolicy: Always`, or nodes serve a
  stale cached image.
- **Flink on a fresh stack** retries until the gateway's first publish
  creates the `api-request` topic (fixed-delay restart strategy). If the
  Flink UI shows `slots-total: 0`, restart the taskmanager.
