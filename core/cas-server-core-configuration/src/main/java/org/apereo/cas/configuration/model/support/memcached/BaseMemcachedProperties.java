package org.apereo.cas.configuration.model.support.memcached;

import org.apereo.cas.configuration.support.RequiredProperty;

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
     * Indicate the transcoder type. Accepted values are {@code KRYO, SERIAL, WHALIN, WHALINV1}.
     * The default is {code KRYO}.
     */
    private String transcoder = "KRYO";

    /**
     * For transcoders other than kryo, determines the compression threshold.
     * Does not apply to kryo.
     */
    private int transcoderCompressionThreshold = 16384;

    /**
     * Comma-separated list of memcached servers.
     */
    @RequiredProperty
    private String servers = "localhost:11211";

    /**
     * Failure mode. Acceptable values are {@code Redistribute,Retry,Cancel}.
     */
    private String failureMode = "Redistribute";

    /**
     * Locator mode. Acceptable values are {@code ARRAY_MOD, CONSISTENT, VBUCKET}.
     */
    private String locatorType = "ARRAY_MOD";

    /**
     * Hash algorithm. Acceptable values are {@code NATIVE_HASH,CRC_HASH,FNV1_64_HASH,FNV1A_64_HASH,FNV1_32_HASH,FNV1A_32_HASH,KETAMA_HASH}.
     */
    private String hashAlgorithm = "FNV1_64_HASH";

    /**
     * Sets the cap on the number of objects that can be allocated by
     * the pool (checked out to clients, or idle awaiting checkout) at a given time. Use a negative value for no limit.
     */
    private int maxTotal = 20;

    /**
     * Set the value for the maxTotal configuration attribute for pools created with this configuration instance.
     */
    private int maxIdle = 8;

    /**
     * Get the value for the minIdle configuration attribute for pools created with this configuration instance.
     */
    private int minIdle;

    /**
     * If true, {@code reset} is called automatically after an entire object graph has been read or written. If
     * false, {@code reset} must be called manually, which allows unregistered class names, references, and other information to
     * span multiple object graphs.
     */
    private boolean kryoAutoReset;

    /**
     * If true, each appearance of an object in the graph after the first is stored as an integer ordinal. When set to true,
     * {@code MapReferenceResolver} is used. This enables references to the same object and cyclic graphs to be serialized, but
     * typically adds overhead of one byte per object.
     */
    private boolean kryoObjectsByReference;

    /**
     * If true, an exception is thrown when an unregistered class is encountered.
     * <p>
     * If false, when an unregistered class is encountered, its fully qualified class name will be serialized and the
     * default serializer for the class used to serialize the object. Subsequent
     * appearances of the class within the same object graph are serialized as an int id.
     * Registered classes are serialized as an int id, avoiding the overhead of serializing the class name, but have the drawback
     * of needing to know the classes to be serialized up front.  See {@code ComponentSerializationPlan} for help here.
     * </p>
     */
    private boolean kryoRegistrationRequired = true;

    public int getTranscoderCompressionThreshold() {
        return transcoderCompressionThreshold;
    }

    public void setTranscoderCompressionThreshold(final int transcoderCompressionThreshold) {
        this.transcoderCompressionThreshold = transcoderCompressionThreshold;
    }

    public boolean isKryoAutoReset() {
        return kryoAutoReset;
    }

    public void setKryoAutoReset(final boolean kryoAutoReset) {
        this.kryoAutoReset = kryoAutoReset;
    }

    public boolean isKryoObjectsByReference() {
        return kryoObjectsByReference;
    }

    public void setKryoObjectsByReference(final boolean kryoObjectsByReference) {
        this.kryoObjectsByReference = kryoObjectsByReference;
    }

    public boolean isKryoRegistrationRequired() {
        return kryoRegistrationRequired;
    }

    public void setKryoRegistrationRequired(final boolean kryoRegistrationRequired) {
        this.kryoRegistrationRequired = kryoRegistrationRequired;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(final int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(final int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(final int minIdle) {
        this.minIdle = minIdle;
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



