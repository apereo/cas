package org.apereo.cas.configuration.model.core.metrics;

import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link MetricsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class MetricsProperties {

    private String refreshInterval = "PT30S";
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
