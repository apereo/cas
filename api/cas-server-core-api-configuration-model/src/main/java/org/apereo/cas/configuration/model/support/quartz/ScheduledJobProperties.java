package org.apereo.cas.configuration.model.support.quartz;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link ScheduledJobProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ScheduledJobProperties implements Serializable {

    private static final long serialVersionUID = 9059671958275130605L;

    /**
     * Scheduler settings to indicate how often the job should run.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties();

    public ScheduledJobProperties(final String startDelay, final String repeatInterval) {
        schedule.setEnabled(true);
        schedule.setStartDelay(startDelay);
        schedule.setRepeatInterval(repeatInterval);
    }
}
