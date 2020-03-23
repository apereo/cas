package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link MonitorWarningProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class MonitorWarningProperties implements Serializable {

    private static final long serialVersionUID = 2788617778375787703L;

    /**
     * The monitor threshold where if reached, CAS might generate a warning status for health checks.
     */
    private int threshold = 10;

    /**
     * The monitor eviction threshold where if reached, CAS might generate a warning status for health checks.
     * The underlying data source and monitor (i.e. cache) must support the concept of evictions.
     */
    private long evictionThreshold;

    public MonitorWarningProperties(final int threshold) {
        this.threshold = threshold;
    }
}
