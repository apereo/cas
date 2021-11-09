package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-throttle")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ThrottleProperties")
public class ThrottleProperties implements Serializable {

    private static final long serialVersionUID = 6813165633105563813L;

    /**
     * Throttling failure events.
     */
    @NestedConfigurationProperty
    private ThrottleFailureProperties failure = new ThrottleFailureProperties();

    /**
     * Record authentication throttling events in a JDBC resource.
     */
    @NestedConfigurationProperty
    private JdbcThrottleProperties jdbc = new JdbcThrottleProperties();

    /**
     * Settings related to throttling requests using bucket4j.
     */
    @NestedConfigurationProperty
    private Bucket4jThrottleProperties bucket4j = new Bucket4jThrottleProperties();

    /**
     * Settings related to throttling requests using hazelcast.
     */
    @NestedConfigurationProperty
    private HazelcastThrottleProperties hazelcast = new HazelcastThrottleProperties();

    /**
     * Core throttling settings.
     */
    @NestedConfigurationProperty
    private ThrottleCoreProperties core = new ThrottleCoreProperties();

    /**
     * Scheduler settings to clean up throttled attempts.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties();

    public ThrottleProperties() {
        schedule.setEnabled(true);
        schedule.setStartDelay("PT10S");
        schedule.setRepeatInterval("PT30S");
    }
}
