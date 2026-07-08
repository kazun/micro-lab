package com.peap.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Rejects requests from client IPs the Flink fraud job has flagged. The
 * job writes blocklist:ip:{ip} keys with a TTL, so blocks expire on their
 * own; this filter only does a sub-millisecond existence check per call.
 * Fails open: if Redis is unreachable, traffic passes rather than taking
 * the whole API down.
 *
 * @author kazun
 */
@Component
public class BlocklistFilter implements GlobalFilter, Ordered {

    static final String KEY_PREFIX = "blocklist:ip:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public BlocklistFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = ApiRequestPublishFilter.ClientIps.resolve(exchange);
        return redisTemplate.hasKey(KEY_PREFIX + clientIp)
                .onErrorReturn(false)
                .flatMap(blocked -> {
                    if (blocked) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
