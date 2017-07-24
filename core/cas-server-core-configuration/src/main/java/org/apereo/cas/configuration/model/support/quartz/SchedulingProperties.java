package org.apereo.cas.configuration.model.support.quartz;

import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link SchedulingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SchedulingProperties {

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

    public long getStartDelay() {
        return Beans.newDuration(startDelay).toMillis();
    }

    public void setStartDelay(final String startDelay) {
        this.startDelay = startDelay;
    }

    public long getRepeatInterval() {
        return Beans.newDuration(repeatInterval).toMillis();
    }

    public void setRepeatInterval(final String repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
