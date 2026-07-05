# PEAP microservices (MVP slice)

Java 17 / Spring Boot 3.3 multi-module Maven project implementing the MVP scope
from the Product Analysis & Initiation Document: registration/login, entity
creation, voting, and basic reviews, fronted by an API gateway and connected
through Kafka.

## Modules

| Module | Port | Owns | Publishes |
|---|---|---|---|
| `api-gateway` | 8080 | routing only | - |
| `identity-service` | 8080 (container) | `identity_db` | `user-created` |
| `product-service` | 8080 (container) | `product_db` | `entity-created` |
| `voting-service` | 8080 (container) | `voting_db` + Redis cache | `vote-submitted` |
| `review-service` | 8080 (container) | `review_db` | `review-submitted` |

Each service owns its own Postgres database — no service queries another's
tables directly. Cross-service reads/writes go through the gateway or Kafka.

## Run locally without Docker

Needs a local Postgres (with `identity_db`, `product_db`, `voting_db`,
`review_db`), Redis, and Kafka reachable at the defaults in each
`application.yml`.

```bash
cd services
mvn -pl identity-service -am spring-boot:run
```

## Run the full stack with Docker Compose

The Dockerfiles copy an already-built jar rather than running Maven inside the
image (running 5 independent `mvn dependency:go-offline` steps with a cold
`~/.m2` cache is slow and, on an unreliable connection, prone to truncated
downloads of large artifacts). Build the jars once on the host, then build
images:

```bash
cd services && mvn -B clean package -DskipTests && cd ..
docker-compose build
docker-compose up -d
```

This starts Kafka, Kafka UI (`:8090`), Postgres, Redis, and all five services.
The gateway is reachable at `http://localhost:8080`; each service is also
exposed directly (`identity-service:8081`, `product-service:8082`,
`voting-service:8083`, `review-service:8084`) for debugging.

## Try it

```bash
# Register a user
curl -X POST localhost:8080/api/v1/users/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.com","password":"password123"}'

# Create an entity
curl -X POST localhost:8080/api/v1/entities \
  -H 'Content-Type: application/json' \
  -d '{"name":"Acme Soap","category":"consumer-products"}'

# Cast a vote (entityId/userId from the responses above)
curl -X POST localhost:8080/api/v1/votes \
  -H 'Content-Type: application/json' \
  -d '{"entityId":"<entity-id>","userId":"<user-id>","value":5}'

# Read the aggregate score
curl localhost:8080/api/v1/votes/entity/<entity-id>/score
```

## What's intentionally not here yet

- **Fraud detection, analytics, notification, search services** — deferred
  per the AID's MVP scope (section 13); they're the Kafka consumers on the
  topics these services already publish to.
- **Gateway-side JWT enforcement / RBAC** — `identity-service` issues tokens
  today; validating them at the gateway and wiring role-based route access is
  the next security milestone.
- **Kubernetes manifests** — this repo previously had a kustomize
  base/overlay example for a single generic Spring Boot app; the same pattern
  needs to be re-applied per service here.
