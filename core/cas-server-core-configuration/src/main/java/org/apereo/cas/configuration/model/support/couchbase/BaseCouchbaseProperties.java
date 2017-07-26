package org.apereo.cas.configuration.model.support.couchbase;

import org.apereo.cas.configuration.support.Beans;

import java.io.Serializable;

/**
 * This is {@link BaseCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseCouchbaseProperties implements Serializable {
    /**
     * Flag to indicate if query is enabled.
     */
    private boolean queryEnabled = true;

    /**
     * Nodeset name.
     */
    private String nodeSet = "localhost:8091";

    /**
     * String representation of connection timeout.
     */
    private String timeout = "PT10S";

    /**
     * Password.
     */
    private String password;

    /**
     * Bucket name.
     */
    private String bucket = "default";

    public boolean isQueryEnabled() {
        return queryEnabled;
    }

    public void setQueryEnabled(final boolean queryEnabled) {
        this.queryEnabled = queryEnabled;
    }

    public String getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(final String nodeSet) {
        this.nodeSet = nodeSet;
    }

    public long getTimeout() {
        return Beans.newDuration(timeout).getSeconds();
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }
}
