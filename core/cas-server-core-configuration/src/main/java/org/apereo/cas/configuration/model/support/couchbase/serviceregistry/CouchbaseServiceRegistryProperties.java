package org.apereo.cas.configuration.model.support.couchbase.serviceregistry;

import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link CouchbaseServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class CouchbaseServiceRegistryProperties {
    private boolean queryEnabled = true;

    private String nodeSet = "localhost:8091";

    private String timeout = "PT10S";

    private String password;
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
