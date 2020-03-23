package org.apereo.cas.configuration.model.support.ignite;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link IgniteProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-ignite-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class IgniteProperties implements Serializable {

    private static final long serialVersionUID = -5259465262649559156L;

    /**
     * Used by {@code TcpDiscoveryVmIpFinder} which is an
     * IP Finder which works only with pre-configured list of IP addresses specified via this setting.
     * By default, this IP finder is not shared, which means that all grid nodes have
     * to be configured with the same list of IP addresses when this IP finder is used.
     * Parses provided values and initializes the internal collection of addresses.
     * Addresses may be represented as follows:
     * <ul>
     * <li>IP address (e.g. 127.0.0.1, 9.9.9.9, etc);</li>
     * <li>IP address and port (e.g. 127.0.0.1:47500, 9.9.9.9:47501, etc);</li>
     * <li>IP address and port range (e.g. 127.0.0.1:47500..47510, 9.9.9.9:47501..47504, etc);</li>
     * <li>Hostname (e.g. host1.com, host2, etc);</li>
     * <li>Hostname and port (e.g. host1.com:47500, host2:47502, etc).</li>
     * <li>Hostname and port range (e.g. host1.com:47500..47510, host2:47502..47508, etc).</li>
     * </ul>
     * If port is 0 or not provided then default port will be used (depends on discovery SPI configuration).
     * If port range is provided (e.g. host:port1..port2) the following should be considered:
     * <ul>
     * <li>port1 &lt; port2 should be true;</li>
     * <li>Both port1 and port2 should be greater than 0.</li>
     * </ul>
     */
    @RequiredProperty
    private List<String> igniteAddress = Stream.of("localhost:47500").collect(Collectors.toList());

    /**
     * Settings related to tickets cache.
     */
    private TicketsCache ticketsCache = new TicketsCache();

    /**
     * Keystore type used to create a SSL context for the ticket registry.
     */
    private String keyStoreType = "JKS";

    /**
     * Keystore file path used to create a SSL context for the ticket registry.
     */
    private String keyStoreFilePath;

    /**
     * Keystore password used to create a SSL context for the ticket registry.
     */
    private String keyStorePassword;

    /**
     * Truststore type used to create a SSL context for the ticket registry.
     */
    private String trustStoreType = "JKS";

    /**
     * SSL protocol used to create a SSL context for the ticket registry.
     */
    private String protocol = "TLS";

    /**
     * The key algorithm to use when creating SSL context.
     */
    private String keyAlgorithm = "SunX509";

    /**
     * Truststore file path used to create a SSL context for the ticket registry.
     */
    private String trustStoreFilePath;

    /**
     * Truststore password used to create a SSL context for the ticket registry.
     */
    private String trustStorePassword;

    /**
     * Sets timeout for receiving acknowledgement for sent message.
     * If acknowledgement is not received within this timeout, sending is considered as failed and SPI tries to repeat message sending.
     */
    private String ackTimeout = "PT2S";

    /**
     * Sets join timeout.
     * If non-shared IP finder is used and node fails to connect to any address from IP finder,
     * node keeps trying to join within this timeout. If all addresses are still unresponsive, exception is thrown and node startup fails.
     */
    private String joinTimeout = "PT1S";

    /**
     * Sets local host IP address that discovery SPI uses.
     * If not provided, by default a first found non-loopback address will be used. If there is
     * no non-loopback address available, then {@code InetAddress.getLocalHost()} will be used.
     */
    private String localAddress;

    /**
     * Sets local port to listen to.
     */
    @RequiredProperty
    private int localPort = -1;

    /**
     * Sets maximum network timeout to use for network operations.
     */
    private String networkTimeout = "PT5S";

    /**
     * Sets socket operations timeout. This timeout is used to limit connection time and write-to-socket time.
     * Note that when running Ignite on Amazon EC2, socket timeout must be set to a value significantly greater than the default (e.g. to 30000).
     */
    private String socketTimeout = "PT5S";

    /**
     * Sets thread priority. All threads within SPI will be started with it.
     */
    private int threadPriority = 10;

    /**
     * By default, Ignite nodes consume up to 20% of the RAM available locally,
     * and in most cases, ​this is the only parameter you might need to change.
     * Using the below setting allows you to change the default region memory size.
     */
    private long defaultRegionMaxSize = 4L * 1024 * 1024 * 1024;

    /**
     * Ignite native persistence is a distributed ACID and SQL-compliant disk store that transparently
     * integrates with Ignite's durable memory. Ignite persistence is optional and can be turned on and off.
     * When turned off Ignite becomes a pure in-memory store.
     * With the native persistence enabled, Ignite always stores a superset of data on disk, and as much as it
     * can in RAM based on the capacity of the latter. For example, if there are 100 entries and RAM has the
     * capacity to store only 20, then all 100 will be stored on disk and only 20 will be cached in RAM for better performance.
     * Also, it is worth mentioning that as with a pure in-memory use case, when the persistence is turned on,
     * every individual cluster node persists only a subset of the data, only including partitions for which the node is
     * either primary or backup. Collectively, the whole cluster contains the full data set.
     */
    private boolean defaultPersistenceEnabled;

    /**
     * Start in client mode.
     * If true the local node is started as a client.
     */
    private boolean clientMode;

    /**
     * Sets force server mode flag.
     * If true {@code TcpDiscoverySpi} is started in server mode regardless of {@code IgniteConfiguration.isClientMode()}.
     */
    private boolean forceServerMode;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public IgniteProperties() {
        this.crypto.setEnabled(false);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-ignite-ticket-registry")
    public static class TicketsCache implements Serializable {

        private static final long serialVersionUID = 4715167757542984471L;

        /**
         * Specified the caching mode.
         * <ul>
         * <li>{@code LOCAL}: Specifies local-only cache behaviour. In this mode caches residing on different grid nodes will not know about each other.
         * Other than distribution, local caches still have all the caching features, such as eviction, expiration, swapping, querying, etc...
         * This mode is very useful when caching read-only data or data that automatically expires at a certain interval and
         * then automatically reloaded from persistence store.</li>
         * <li>
         * {@code REPLICATED}: Specifies fully replicated cache behavior. In this mode all the keys are distributed to all participating nodes.
         * User still has affinity control over subset of nodes for any given key via AffinityFunction configuration.
         * </li>
         * <li>
         * {@code PARTITIONED}: Specifies partitioned cache behaviour. In this mode the overall key set will be divided into
         * partitions and all partitions will be split equally between participating nodes. User has affinity control over key
         * assignment via AffinityFunction configuration.
         * Note that partitioned cache is always fronted by local 'near' cache which stores most recent data. You can configure the
         * size of near cache via NearCacheConfiguration.getNearEvictionPolicy() configuration property.
         * </li>
         * </ul>
         */
        private String cacheMode = "REPLICATED";

        /**
         * Specifies the atomicity mode.
         * <ul>
         * <li>{@code ATOMIC}: Specifies atomic-only cache behaviour. In this mode distributed transactions and distributed locking
         * are not supported. Disabling transactions and locking allows to achieve much higher performance and throughput ratios.
         * In addition to transactions and locking, one of the main differences in ATOMIC mode is that bulk writes, such as putAll(...),
         * removeAll(...), and transformAll(...) methods, become simple batch operations which can partially fail. In case of
         * partial failure CachePartialUpdateCheckedException will be thrown which will contain a list of keys for which the update
         * failed. It is recommended that bulk writes are used whenever multiple keys need to be inserted or updated in cache,
         * as they reduce number of network trips and provide better performance.
         * Note that even without locking and transactions, ATOMIC mode still provides full consistency guarantees across all cache nodes.
         * Also note that all data modifications in ATOMIC mode are guaranteed to be atomic and consistent with writes
         * to the underlying persistent store, if one is configured.</li>
         * <li>{@code TRANSACTIONAL}: Specifies fully ACID-compliant transactional cache behavior.</li>
         * </ul>
         */
        private String atomicityMode = "TRANSACTIONAL";

        /**
         * Mode indicating how Ignite should wait for write replies from other nodes.
         * Default value is FULL_ASYNC}, which means that Ignite will not wait for responses from participating nodes. This means that by default remote
         * nodes may get their state updated slightly after any of the cache write methods complete, or after Transaction.commit() method completes.
         * <ul>
         * <li>
         * {@code FULL_ASYNC}: Flag indicating that Ignite will not wait for write or commit responses from participating nodes, which means that
         * remote nodes may get their state updated a bit after any of the cache write methods complete,
         * or after {@code Transaction.commit()} method completes.
         * </li>
         * <li>
         * {@code FULL_SYNC}: Flag indicating that Ignite should wait for write or commit replies from all nodes. This behavior guarantees
         * that whenever any of the atomic or transactional writes complete, all other participating nodes which
         * cache the written data have been updated.
         * </li>
         * <li>
         * {@code PRIMARY_SYNC}: This flag only makes sense for CacheMode.PARTITIONED mode. When enabled, Ignite will wait for write or
         * commit to complete on primary node, but will not wait for backups to be updated.
         * </li>
         * </ul>
         */
        private String writeSynchronizationMode = "FULL_SYNC";
    }
}
