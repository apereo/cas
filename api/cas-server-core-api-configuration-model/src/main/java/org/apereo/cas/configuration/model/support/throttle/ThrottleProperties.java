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

    /**
     * Default app code for throttling and audits.
     */
    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    /**
     * Default authentication failed action used as the code.
     */
    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";

    private static final long serialVersionUID = 6813165633105563813L;

    /**
     * Throttling failure events.
     */
    private Failure failure = new Failure();

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
     * Username parameter to use in order to extract the username from the request.
     */
    private String usernameParameter;

    /**
     * Application code used to identify this application in the audit logs.
     */
    private String appCode = DEFAULT_APPLICATION_CODE;

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

    /**
     * Failure.
     */
    @RequiresModule(name = "cas-server-support-throttle")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Failure implements Serializable {

        private static final long serialVersionUID = 1246256695801461610L;

        /**
         * Failure code to record in the audit log.
         * Generally this indicates an authentication failure event.
         */
        private String code = DEFAULT_AUTHN_FAILED_ACTION;

        /**
         * Number of failed login attempts permitted in the above period.
         * All login throttling components that ship with CAS limit successive failed
         * login attempts that exceed a threshold rate in failures per second.
         */
        private int threshold = -1;

        /**
         * Period of time in seconds during which the threshold applies.
         */
        private int rangeSeconds = -1;
    }

}
