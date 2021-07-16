package org.apereo.cas.configuration.model.support.quartz;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("SchedulingProperties")
public class SchedulingProperties implements Serializable {

    private static final long serialVersionUID = -1522227059439367394L;

    /**
     * Whether scheduler should be enabled to schedule the job to run.
     */
    private boolean enabled = true;

    /**
     * Overrides {@link SchedulingProperties#enabled} property value of true if this property does not match hostname of CAS server.
     * This can be useful if deploying CAS with an image in a statefulset where all names are predictable but
     * where having different configurations for different servers is hard. The value can be an exact hostname
     * or a regular expression that will be used to match the hostname.
     */
    private String enabledOnHost = ".*";

    /**
     * String representation of a start delay of loading data for a data store implementation.
     * This is the delay between scheduler startup and first job’s execution
     */
    @DurationCapable
    private String startDelay = "PT15S";

    /**
     * String representation of a repeat interval of re-loading data for an data store implementation.
     * This is the timeout between consecutive job’s executions.
     */
    @DurationCapable
    private String repeatInterval = "PT2M";
}
