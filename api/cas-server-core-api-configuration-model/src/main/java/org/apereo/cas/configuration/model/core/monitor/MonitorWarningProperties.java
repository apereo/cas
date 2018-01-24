package org.apereo.cas.configuration.model.core.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * This is {@link MonitorWarningProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Slf4j
@Getter
@Setter
@NoArgsConstructor
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
