package org.apereo.cas.configuration.model.support.memcached;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseMemcachedProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-memcached-core")
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
}
