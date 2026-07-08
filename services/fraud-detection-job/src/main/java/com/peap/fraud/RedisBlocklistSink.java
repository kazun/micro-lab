package com.peap.fraud;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

/**
 * Writes flagged client IPs into the blocklist the api-gateway checks on
 * every request. SETEX with a TTL means blocks expire on their own - no
 * cleanup process needed, and re-detection simply refreshes the window.
 *
 * @author kazun
 */
public class RedisBlocklistSink extends RichSinkFunction<FraudAlert> {

    private static final Logger log = LoggerFactory.getLogger(RedisBlocklistSink.class);

    private final String redisHost;
    private final int redisPort;
    private final long blockTtlSeconds;

    private transient JedisPooled jedis;

    public RedisBlocklistSink(String redisHost, int redisPort, long blockTtlSeconds) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.blockTtlSeconds = blockTtlSeconds;
    }

    @Override
    public void open(Configuration parameters) {
        jedis = new JedisPooled(redisHost, redisPort);
    }

    @Override
    public void invoke(FraudAlert alert, Context context) {
        String key = "blocklist:ip:" + alert.clientIp;
        jedis.setex(key, blockTtlSeconds, alert.reason);
        log.info("blocked {} for {}s: {}", alert.clientIp, blockTtlSeconds, alert.reason);
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
