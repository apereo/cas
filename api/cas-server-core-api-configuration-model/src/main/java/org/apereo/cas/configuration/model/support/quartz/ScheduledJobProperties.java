package org.apereo.cas.configuration.model.support.quartz;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * This is {@link ScheduledJobProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Slf4j
@Getter
@Setter
@NoArgsConstructor
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
