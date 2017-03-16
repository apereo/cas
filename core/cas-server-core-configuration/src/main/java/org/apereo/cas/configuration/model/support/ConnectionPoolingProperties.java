package org.apereo.cas.configuration.model.support;

import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link ConnectionPoolingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ConnectionPoolingProperties {
    private int minSize = 6;
    private int maxSize = 18;
    private String maxWait = "PT2S";
    private boolean suspension;
    private long timeoutMillis = 1_000;
    
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(final long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public boolean isSuspension() {
        return suspension;
    }

    public void setSuspension(final boolean suspension) {
        this.suspension = suspension;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(final int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
    }

    public long getMaxWait() {
        return Beans.newDuration(maxWait).toMillis();
    }

    public void setMaxWait(final String maxWait) {
        this.maxWait = maxWait;
    }
}
