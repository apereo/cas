package org.apereo.cas.configuration.model.support.memcached;

/**
 * This is {@link MemcachedProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class MemcachedProperties {
    
    private String servers = "localhost:11211}";
    private String failureMode = "Redistribute";
    private String locatorType = "ARRAY_MOD";
    private String hashAlgorithm = "FNV1_64_HASH";

    public String getServers() {
        return servers;
    }

    public void setServers(final String servers) {
        this.servers = servers;
    }

    public String getFailureMode() {
        return failureMode;
    }

    public void setFailureMode(final String failureMode) {
        this.failureMode = failureMode;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public void setLocatorType(final String locatorType) {
        this.locatorType = locatorType;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(final String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }
}



