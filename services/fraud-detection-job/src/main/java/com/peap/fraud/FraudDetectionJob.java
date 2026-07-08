package com.peap.fraud;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * Traffic-level fraud detection over the gateway's api-request events.
 *
 * Rule 1 (request flood): keyBy client IP, sliding processing-time window,
 * flag any IP exceeding REQUEST_THRESHOLD requests per WINDOW_SECONDS.
 * Alerts go to the fraud-detected Kafka topic and the Redis blocklist the
 * gateway enforces.
 *
 * Processing time (not event time) keeps the demo robust: with a single
 * low-traffic partition, event-time watermarks stall between requests and
 * windows never fire.
 *
 * @author kazun
 */
public class FraudDetectionJob {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws Exception {
        String kafkaBootstrap = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String redisHost = env("REDIS_HOST", "localhost");
        int redisPort = Integer.parseInt(env("REDIS_PORT", "6379"));
        long threshold = Long.parseLong(env("REQUEST_THRESHOLD", "30"));
        long windowSeconds = Long.parseLong(env("WINDOW_SECONDS", "60"));
        long slideSeconds = Long.parseLong(env("SLIDE_SECONDS", "10"));
        long blockTtlSeconds = Long.parseLong(env("BLOCK_TTL_SECONDS", "300"));

        StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaBootstrap)
                .setTopics("api-request")
                .setGroupId("fraud-detection")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<ApiRequest> requests = execEnv
                .fromSource(source, WatermarkStrategy.noWatermarks(), "api-request-source")
                .flatMap((String json, Collector<ApiRequest> out) -> {
                    try {
                        out.collect(MAPPER.readValue(json, ApiRequest.class));
                    } catch (Exception ignored) {
                        // malformed event - skip rather than fail the job
                    }
                })
                .returns(ApiRequest.class);

        DataStream<FraudAlert> alerts = requests
                .keyBy(r -> r.clientIp == null ? "unknown" : r.clientIp)
                .window(SlidingProcessingTimeWindows.of(
                        Duration.ofSeconds(windowSeconds), Duration.ofSeconds(slideSeconds)))
                .process(new RequestFloodDetector(threshold));

        alerts.addSink(new RedisBlocklistSink(redisHost, redisPort, blockTtlSeconds))
                .name("redis-blocklist");

        KafkaSink<String> fraudTopicSink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaBootstrap)
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("fraud-detected")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .build();

        alerts.map(FraudDetectionJob::toJson).sinkTo(fraudTopicSink).name("fraud-detected-topic");

        execEnv.execute("peap-fraud-detection");
    }

    /**
     * Counts requests per key in the window; emits an alert only when the
     * threshold is breached.
     */
    static class RequestFloodDetector
            extends ProcessWindowFunction<ApiRequest, FraudAlert, String, TimeWindow> {

        private final long threshold;

        RequestFloodDetector(long threshold) {
            this.threshold = threshold;
        }

        @Override
        public void process(
                String clientIp, Context context, Iterable<ApiRequest> events, Collector<FraudAlert> out) {
            long count = 0;
            for (ApiRequest ignored : events) {
                count++;
            }
            if (count > threshold) {
                out.collect(new FraudAlert("request-flood", clientIp, count, context.window().getEnd()));
            }
        }
    }

    private static String toJson(FraudAlert alert) {
        try {
            return MAPPER.writeValueAsString(alert);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
