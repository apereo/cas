package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link BaseRedisProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-redis-core")
@Accessors(chain = true)
public class BaseRedisProperties implements Serializable, CasFeatureModule {

    @Serial
    private static final long serialVersionUID = -2600996981339638782L;

    /**
     * Whether the module is enabled or not, defaults to true.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * Database URI.
     */
    private String uri;

    /**
     * Database index used by the connection factory.
     */
    @RequiredProperty
    private int database;

    /**
     * Redis server host.
     */
    @RequiredProperty
    private String host = "localhost";

    /**
     * Login username of the redis server.
     */
    @RequiredProperty
    private String username;

    /**
     * Login password of the redis server.
     */
    @RequiredProperty
    private String password;

    /**
     * Redis server port.
     */
    @RequiredProperty
    private int port = 6379;

    /**
     * Command timeout.
     */
    @DurationCapable
    private String timeout = "PT60S";

    /**
     * Redis connection pool settings.
     */
    @NestedConfigurationProperty
    private RedisPoolProperties pool = new RedisPoolProperties();

    /**
     * Redis Sentinel settings.
     */
    @NestedConfigurationProperty
    private RedisSentinelProperties sentinel = new RedisSentinelProperties();

    /**
     * Redis cluster settings.
     */
    @NestedConfigurationProperty
    private RedisClusterProperties cluster = new RedisClusterProperties();

    /**
     * Redis scan count option. When and if specified, SCAN operations would be "counted" or limited by this setting.
     * While SCAN does not provide guarantees about the number of elements returned
     * at every iteration, it is possible to empirically adjust the behavior
     * of SCAN using the COUNT option. Basically with COUNT the user specified
     * the amount of work that should be done at every call in order to retrieve
     * elements from the collection. This is just a hint for the implementation,
     * however generally speaking this is what you could expect most of the times from the implementation.
     */
    private long scanCount;

    /**
     * Whether or not to use SSL for connection factory.
     */
    private boolean useSsl;

    /**
     * The shared native connection is never closed by Lettuce connection, therefore it is not validated by default when connections are retrieved.
     * If this setting is {@code true}, a shared connection will be used for regular operations and
     * a connection provider will be used to select a connection for blocking and tx operations only, which
     * should not share a connection. If native connection sharing is disabled, new (or pooled) connections will be used for all operations.
     * By default, multiple connections share a single thread-safe native connection. If you enable connection pooling,
     * then native connection sharing will be disabled and the connection pool will be used for all operations.
     * You may however explicitly control connection sharing via this setting as an override.
     */
    private Boolean shareNativeConnections;

    /**
     * Redis protocol version.
     */
    private String protocolVersion = "RESP3";

    /**
     * Connection timeout.
     */
    @DurationCapable
    private String connectTimeout = "PT10S";

    /**
     * Setting that describes how Lettuce routes read operations to replica nodes.
     * Note that modes referencing MASTER/SLAVE are deprecated (but still supported) in the Lettuce redis client dependency
     * so migrate config to UPSTREAM/REPLICA.
     */
    private RedisReadFromTypes readFrom;

    /**
     * Control how peer verification is handled with redis connections.
     * Peer verification is a security feature that checks if the host you're
     * connecting to is who it says it is. This is often done by checking a digital certificate.
     */
    private boolean verifyPeer = true;

    /**
     * Start mutual TLS.
     * In order to support TLS, Redis should be configured with a X.509 certificate and a private key.
     * In addition, it is necessary to specify a CA certificate bundle file or path to be used
     * as a trusted root when validating certificates.
     */
    private boolean startTls;

    /**
     * May be used when making SSL connections to build the key manager.
     * Sets the key certificate file to use for client authentication.
     * This is typically an {@code X.509} certificate file (or chain file) in PEM format.
     */
    private File keyCertificateChainFile;

    /**
     * May be used when making SSL connections to build the trust manager.
     * Sets the certificate file to use for client authentication.
     * This is typically an {@code X.509} certificate file (or chain file) in PEM format.
     */
    private File certificateFile;

    /**
     * May be used when making SSL connections.
     * Sets the key file for client authentication.
     * The key is reloaded on each connection attempt that allows CAS to replace certificates during runtime.
     * This is typically a {@code PKCS#8} private key file in PEM format.
     */
    private File keyFile;

    /**
     * The password of the {@link #keyFile}, or {@code null} if it's not password-protected.
     */
    private String keyPassword;

    /**
     * The Lettuce library {@code ReadFrom} types that determine how Lettuce routes read operations to replica nodes.
     */
    public enum RedisReadFromTypes {
        /**
         * Read from the current upstream node.
         */
        UPSTREAM,
        /**
         * Read from the upstream node, but if it is unavailable, read from replica nodes.
         */
        UPSTREAMPREFERRED,
        /**
         * Read from the current upstream node.
         *
         * @deprecated Use {@link BaseRedisProperties.RedisReadFromTypes#UPSTREAM} instead.
         */
        @Deprecated
        MASTER,
        /**
         * Read from the upstream node, but if it is unavailable, read from replica nodes.
         *
         * @deprecated Use {@link BaseRedisProperties.RedisReadFromTypes#UPSTREAMPREFERRED} instead.
         */
        @Deprecated
        MASTERPREFERRED,
        /**
         * Read from replica nodes.
         *
         * @deprecated Use {@link BaseRedisProperties.RedisReadFromTypes#REPLICA} instead.
         */
        @Deprecated
        SLAVE,
        /**
         * Read from the replica nodes, but if none is unavailable, read from the upstream node.
         *
         * @deprecated Use {@link BaseRedisProperties.RedisReadFromTypes#REPLICAPREFERRED} instead.
         */
        @Deprecated
        SLAVEPREFERRED,
        /**
         * Read from replica nodes.
         */
        REPLICA,
        /**
         * Read from the replica nodes, but if none is unavailable, read from the upstream node.
         */
        REPLICAPREFERRED,
        /**
         * Read from any node of the cluster.
         */
        ANY,
        /**
         * Read from any replica node of the cluster.
         */
        ANYREPLICA,
        /**
         * Read from the nearest node.
         */
        NEAREST
    }
}
