package org.apereo.cas.configuration.model.support.memcached;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link MemcachedTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MemcachedTicketRegistryProperties {
    
    private int maxTotal = 20;
    private int minIdle = 2;
    private int maxIdle = 8;
    
    private String servers = "localhost:11211";
    private String failureMode = "Redistribute";
    private String locatorType = "ARRAY_MOD";
    private String hashAlgorithm = "FNV1_64_HASH";

    @NestedConfigurationProperty
    private CryptographyProperties crypto = new CryptographyProperties();

    public CryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final CryptographyProperties crypto) {
        this.crypto = crypto;
    }
    
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

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(final int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(final int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(final int maxIdle) {
        this.maxIdle = maxIdle;
    }
}



