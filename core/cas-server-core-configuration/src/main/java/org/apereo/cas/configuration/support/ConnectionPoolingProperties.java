package org.apereo.cas.configuration.support;

/**
 * This is {@link ConnectionPoolingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ConnectionPoolingProperties {
    private int minSize = 6;
    private int maxSize = 18;
    private int maxWait = 2_000;
    private boolean suspension;

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

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(final int maxWait) {
        this.maxWait = maxWait;
    }
}
