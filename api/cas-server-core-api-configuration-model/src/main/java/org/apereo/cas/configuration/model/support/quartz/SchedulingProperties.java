package org.apereo.cas.configuration.model.support.quartz;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SchedulingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class SchedulingProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -1522227059439367394L;

    /**
     * Whether scheduler should be enabled to schedule the job to run.
     */
    private boolean enabled = true;

    /**
     * Overrides {@link SchedulingProperties#enabled} property value of true
     * if this property does not match hostname of CAS server.
     * This can be useful if deploying CAS with an image in a statefulset
     * where all names are predictable but
     * where having different configurations for different servers
     * is hard. The value can be an exact hostname
     * or a regular expression that will be used to match the hostname.
     */
    @RegularExpressionCapable
    private String enabledOnHost = ".*";

    /**
     * String representation of a start delay of loading data for a data store implementation.
     * This is the delay between scheduler startup and first job’s execution
     */
    @DurationCapable
    private String startDelay = "PT15S";

    /**
     * String representation of a repeat interval of re-loading data for a data store implementation.
     * This is the timeout between consecutive job’s executions.
     */
    @DurationCapable
    private String repeatInterval = "PT2M";

    /**
     * A cron-like expression, extending the usual UN*X definition to include triggers
     * on the second, minute, hour, day of month, month, and day of week.
     * For example, {@code 0 * * * * MON-FRI} means once per minute on weekdays (at the top of the minute - the 0th second)
     * or {@code 0 0 0 * * *} means every day at midnight.
     * * Note that when cron expressions are defined, the start delay and repeat interval settings are must be
     * removed and set to blank.
     */
    private String cronExpression;

    /**
     * A time zone for which the cron expression will be resolved.
     * By default, this attribute is empty (i.e. the scheduler's time zone will be used).
     */
    private String cronTimeZone;
}
