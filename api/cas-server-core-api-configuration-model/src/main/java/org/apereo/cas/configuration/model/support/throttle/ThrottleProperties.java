package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.RequiresModule;

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
@RequiresModule(name = "cas-server-support-throttle", automated = true)
@Getter
@Setter
@Accessors(chain = true)
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
    private Jdbc jdbc = new Jdbc();

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
    @RequiresModule(name = "cas-server-support-throttle", automated = true)
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

    @RequiresModule(name = "cas-server-support-throttle-jdbc")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jdbc extends AbstractJpaProperties {

        /**
         * SQL throttling query.
         */
        private static final String SQL_AUDIT_QUERY = "SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? "
            + "AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC";

        private static final long serialVersionUID = -9199878384425691919L;

        /**
         * Audit query to execute against the database
         * to locate audit records based on IP, user, date and
         * an application code along with the relevant audit action.
         */
        private String auditQuery = SQL_AUDIT_QUERY;
    }
}
