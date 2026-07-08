package com.peap.fraud;

/**
 * Emitted when a fraud rule fires: published to the fraud-detected Kafka
 * topic for downstream consumers (notification, moderation, analytics)
 * and written to the Redis blocklist for gateway enforcement.
 *
 * @author kazun
 */
public class FraudAlert {

    public String rule;
    public String clientIp;
    public long requestCount;
    public long windowEndEpochMs;
    public String reason;

    public FraudAlert() {
    }

    public FraudAlert(String rule, String clientIp, long requestCount, long windowEndEpochMs) {
        this.rule = rule;
        this.clientIp = clientIp;
        this.requestCount = requestCount;
        this.windowEndEpochMs = windowEndEpochMs;
        this.reason = rule + ": " + requestCount + " requests in window";
    }
}
