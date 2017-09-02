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
     * Set to false if the default operation optimization is not desirable.
     */
    private boolean shouldOptimize;

    /**
     * Set the daemon state of the IO thread (defaults to true).
     */
    private boolean daemon = true;

    /**
     * Set the maximum reconnect delay.
     */
    private long maxReconnectDelay = -1;

    /**
     * Set to true if you'd like to enable the Nagle algorithm.
     */
    private boolean useNagleAlgorithm;

    /**
     * The number of seconds to wait for connections to finish before shutting
     * down the client.
     */
    private long shutdownTimeoutSeconds = -1;

    /**
     * Set the maximum timeout exception threshold.
     */
    private int timeoutExceptionThreshold = 2;

    /**
     * Set the default operation timeout in milliseconds.
     */
    private long opTimeout = -1;
    
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

    public boolean isShouldOptimize() {
        return shouldOptimize;
    }

    public void setShouldOptimize(final boolean shouldOptimize) {
        this.shouldOptimize = shouldOptimize;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(final boolean daemon) {
        this.daemon = daemon;
    }

    public long getMaxReconnectDelay() {
        return maxReconnectDelay;
    }

    public void setMaxReconnectDelay(final long maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
    }

    public boolean isUseNagleAlgorithm() {
        return useNagleAlgorithm;
    }

    public void setUseNagleAlgorithm(final boolean useNagleAlgorithm) {
        this.useNagleAlgorithm = useNagleAlgorithm;
    }

    public long getShutdownTimeoutSeconds() {
        return shutdownTimeoutSeconds;
    }

    public void setShutdownTimeoutSeconds(final long shutdownTimeoutSeconds) {
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
    }

    public int getTimeoutExceptionThreshold() {
        return timeoutExceptionThreshold;
    }

    public void setTimeoutExceptionThreshold(final int timeoutExceptionThreshold) {
        this.timeoutExceptionThreshold = timeoutExceptionThreshold;
    }

    public long getOpTimeout() {
        return opTimeout;
    }

    public void setOpTimeout(final long opTimeout) {
        this.opTimeout = opTimeout;
    }
}



