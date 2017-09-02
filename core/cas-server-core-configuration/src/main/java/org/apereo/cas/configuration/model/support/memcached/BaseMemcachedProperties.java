package org.apereo.cas.configuration.model.support.memcached;

import java.io.Serializable;

/**
 * This is {@link BaseMemcachedProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class BaseMemcachedProperties implements Serializable {

    private static final long serialVersionUID = 514520518053691666L;

    /**
     * Indicate the transcoder type. Accepted values are {@code KRYO,SERIAL}.
     * The default is {code KRYO}.
     */
    private String transcoder = "KRYO";
    
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

    public String getTranscoder() {
        return transcoder;
    }

    public void setTranscoder(final String transcoder) {
        this.transcoder = transcoder;
    }
}



