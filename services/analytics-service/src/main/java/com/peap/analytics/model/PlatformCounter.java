package com.peap.analytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Platform-wide running totals (users registered, entities created, votes
 * cast, reviews submitted), keyed by metric name.
 *
 * @author kazun
 */
@Entity
@Table(name = "platform_counters")
public class PlatformCounter {

    @Id
    @Column(name = "metric_name")
    private String metricName;

    @Column(nullable = false)
    private long value;

    protected PlatformCounter() {
    }

    public PlatformCounter(String metricName) {
        this.metricName = metricName;
        this.value = 0;
    }

    public String getMetricName() {
        return metricName;
    }

    public long getValue() {
        return value;
    }

    public void increment() {
        this.value += 1;
    }
}
