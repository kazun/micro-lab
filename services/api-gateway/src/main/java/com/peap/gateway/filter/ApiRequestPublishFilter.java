package com.peap.gateway.filter;

import com.peap.gateway.event.ApiRequestEvent;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Publishes an api-request event for every call after its response
 * completes. Fire-and-forget: a Kafka hiccup must never fail or slow the
 * request itself, so send errors are only logged.
 *
 * @author kazun
 */
@Component
public class ApiRequestPublishFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestPublishFilter.class);
    private static final String TOPIC = "api-request";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ApiRequestPublishFilter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startMs = System.currentTimeMillis();
        return chain.filter(exchange).doFinally(signal -> {
            try {
                ApiRequestEvent event = new ApiRequestEvent(
                        exchange.getRequest().getMethod().name(),
                        exchange.getRequest().getPath().value(),
                        Optional.ofNullable(exchange.getResponse().getStatusCode())
                                .map(HttpStatusCode::value)
                                .orElse(0),
                        ClientIps.resolve(exchange),
                        exchange.getRequest().getHeaders().getFirst("User-Agent"),
                        System.currentTimeMillis() - startMs,
                        Instant.now());
                kafkaTemplate.send(TOPIC, event.clientIp(), event);
            } catch (Exception e) {
                log.warn("failed to publish api-request event: {}", e.getMessage());
            }
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Shared client-IP resolution for gateway filters. Prefers the first
     * X-Forwarded-For hop when present (set by proxies/LBs that operate at
     * L7), falling back to the socket address.
     */
    static final class ClientIps {

        private ClientIps() {
        }

        static String resolve(ServerWebExchange exchange) {
            String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(InetSocketAddress::getAddress)
                    .map(a -> a.getHostAddress())
                    .orElse("unknown");
        }
    }
}
