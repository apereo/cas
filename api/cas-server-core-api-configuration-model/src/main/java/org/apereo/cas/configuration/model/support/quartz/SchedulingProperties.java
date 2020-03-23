package org.apereo.cas.configuration.model.support.quartz;

import org.apereo.cas.configuration.support.RequiresModule;

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
public class SchedulingProperties implements Serializable {

    private static final long serialVersionUID = -1522227059439367394L;

    /**
     * Whether scheduler is should schedule the job to run.
     */
    private boolean enabled = true;

    /**
     * String representation of a start delay of loading data for a data store implementation.
     * This is the delay between scheduler startup and first job’s execution
     */
    private String startDelay = "PT15S";

    /**
     * String representation of a repeat interval of re-loading data for an data store implementation.
     * This is the timeout between consecutive job’s executions.
     */
    private String repeatInterval = "PT2M";
}
