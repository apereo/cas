package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link MemoryMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class MemoryMonitorProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -7147060071480971606L;

    /**
     * The free memory threshold for the memory monitor.
     * If the amount of free memory available reaches this point
     * the memory monitor will report back a warning status as a health check.
     */
    private double freeMemThreshold = 10;
}
