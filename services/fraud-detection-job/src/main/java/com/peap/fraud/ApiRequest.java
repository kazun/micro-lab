package com.peap.fraud;

/**
 * Flink-side view of the gateway's api-request event. Follows Flink POJO
 * rules (public no-arg constructor, public fields) so it serializes
 * efficiently between operators. Only the fields the fraud rules need are
 * mapped; unknown JSON fields are ignored at parse time.
 *
 * @author kazun
 */
public class ApiRequest {

    public String method;
    public String path;
    public int status;
    public String clientIp;
    public String userAgent;
    public long durationMs;

    public ApiRequest() {
    }
}
