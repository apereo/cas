package org.apereo.cas.configuration.model.support.memcached;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link MemcachedTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MemcachedTicketRegistryProperties implements Serializable {

    private static final long serialVersionUID = 509520518053691786L;
    /**
     * Comma-separated list of memcached servers.
     */
    private String servers = "localhost:11211";
    /**
     * Failure mode. Acceptable values are {@code Redistribute,Retry,Cancel}.
     */
    private String failureMode = "Redistribute";
    /**
     * Locator mode. Acceptable values are {@code ARRAY_MOD,CONSISTENT,VBUCKET}.
     */
    private String locatorType = "ARRAY_MOD";
    /**
     * Hash algorithm. Acceptable values are {@code NATIVE_HASH,CRC_HASH,FNV1_64_HASH,FNV1A_64_HASH,FNV1_32_HASH,FNV1A_32_HASH,KETAMA_HASH}.
     */
    private String hashAlgorithm = "FNV1_64_HASH";

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
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
}



