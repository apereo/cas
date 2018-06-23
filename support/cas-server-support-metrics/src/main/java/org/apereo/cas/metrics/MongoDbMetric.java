package org.apereo.cas.metrics;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.actuate.metrics.Metric;

import java.io.Serializable;
import java.util.Date;

/**
 * This is {@link MongoDbMetric}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@ToString
public class MongoDbMetric implements Serializable {

    private static final long serialVersionUID = 8587687286389110789L;

    private final String name;
    private final Number value;
    private final Date timestamp;

    public MongoDbMetric(final Metric metric) {
        this.name = metric.getName();
        this.value = metric.getValue();
        this.timestamp = metric.getTimestamp();
    }
}
