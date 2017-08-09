package org.apereo.cas.configuration.model.core.metrics;

import org.apereo.cas.configuration.support.Beans;

import java.io.Serializable;

/**
 * This is {@link MetricsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class MetricsProperties implements Serializable {

    private static final long serialVersionUID = 345002357523418414L;
    /**
     * String representation of refresh interval for metrics collection.
     */
    private String refreshInterval = "PT30S";

    /**
     * Log destination name of the logging system in use for metrics output.
     */
    private String loggerName = "perfStatsLogger";

    public long getRefreshInterval() {
        return Beans.newDuration(this.refreshInterval).getSeconds();
    }

    public void setRefreshInterval(final String refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(final String loggerName) {
        this.loggerName = loggerName;
    }
}
