package org.apereo.cas.configuration.model.support.ehcache;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link EhcacheProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.2
 */
@RequiresModule(name = "cas-server-support-ehcache-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "6.2.0")
public class EhcacheProperties implements Serializable {

    private static final long serialVersionUID = 7772510035918976450L;

    /**
     * Enabled allows this registry to be disabled on startup (so registry choice can be made at runtime).
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * Sets the persistence write mode.
     */
    private boolean synchronousWrites;

    /**
     * Whether to load the cache bootstrapper asynchronously.
     */
    private boolean loaderAsync = true;

    /**
     * The maximum serialized size of the elements to request
     * from a remote cache peer during bootstrap.
     */
    private int maxChunkSize = 5000000;

    /**
     * Maximum batch size for replication ops.
     */
    private int maximumBatchSize = 100;

    /**
     * The replication interval in milliseconds for the cache replicator.
     */
    private String replicationInterval = "PT10S";

    /**
     * Whether to replicate puts.
     */
    private boolean replicatePuts = true;

    /**
     * Whether an update (a put) should be by copy or by invalidation, (a remove).
     * By copy is best when the entry is expensive to produce. By invalidation is best when we are
     * really trying to force other caches to sync back to a canonical source like a database.
     * An example of a latter usage would be a read/write cache being used in Hibernate.
     * This setting only has effect if #replicateUpdates is true.
     */
    private boolean replicateUpdatesViaCopy = true;

    /**
     * Whether to replicate removes.
     */
    private boolean replicateRemovals = true;

    /**
     * Whether to replicate updates.
     */
    private boolean replicateUpdates = true;

    /**
     * Whether a put should replicated by copy or by invalidation, (a remove).
     * By copy is best when the entry is expensive to produce. By invalidation is best when we are really
     * trying to force other caches to sync back to a canonical source like a database.
     * An example of a latter usage would be a read/write cache being used in Hibernate.
     * This setting only has effect if #replicateUpdates is true.
     */
    private boolean replicatePutsViaCopy = true;

    /**
     * Set the location of the EhCache config file. A typical value is "/WEB-INF/ehcache.xml".
     * Default is "ehcache.xml" in the root of the class path,
     * or if not found, "ehcache-failsafe.xml" in the EhCache jar (default EhCache initialization).
     */
    @RequiredProperty
    private transient Resource configLocation = new ClassPathResource("ehcache-replicated.xml");

    /**
     * Set whether the EhCache CacheManager should be shared (as a singleton at the ClassLoader level)
     * or independent (typically local within the application). Default is "false", creating an independent local instance.
     * NOTE: This feature allows for sharing this EhCacheManagerFactoryBean's CacheManager with any
     * code calling CacheManager.create() in the same ClassLoader space, with no need to agree on a specific
     * CacheManager name. However, it only supports a single
     * EhCacheManagerFactoryBean involved which will control the lifecycle of
     * the underlying CacheManager (in particular, its shutdown).
     */
    private boolean shared;

    /**
     * The name of the cache manager instance.
     */
    @RequiredProperty
    private String cacheManagerName = "ticketRegistryCacheManager";

    /**
     * The interval in seconds between runs of the disk expiry thread.
     */
    private int diskExpiryThreadIntervalSeconds;

    /**
     * Sets whether elements are eternal.
     * If eternal, timeouts are ignored and the element is never expired. False by default.
     */
    private boolean eternal;

    /**
     * Builder that sets the maximum objects to be held in memory (0 = no limit).
     */
    private int maxElementsInMemory = 10_000;

    /**
     * Builder which sets the maximum number entries in cache.
     */
    private int maxElementsInCache;

    /**
     * Builder which sets the maximum number elements on Disk. 0 means unlimited.
     */
    private int maxElementsOnDisk;

    /**
     * Builder which Sets the eviction policy. An invalid argument will set it to null.
     * <ul>
     * <li>LRU - least recently used</li>
     * <li>LFU - least frequently used</li>
     * <li>FIFO - first in first out, the oldest element by creation time</li>
     * </ul>
     */
    private String memoryStoreEvictionPolicy = "LRU";

    /**
     * Sets the persistence strategy.
     * Acceptable values are:
     * <ul>
     * <li>LOCALTEMPSWAP: Standard open source (non fault-tolerant) on-disk persistence.</li>
     * <li>DISTRIBUTED: Terracotta clustered persistence (requires a Terracotta clustered cache)</li>
     * <li>LOCALRESTARTABLE: Enterprise fault tolerant persistence</li>
     * <li>NONE: No persistence</li>
     * </ul>
     */
    private String persistence = "NONE";

    /**
     * Allows system properties to be set prior to ehcache.xml parsing.
     * EhCache will interpolate system properties in the ehcache xml config file e.g. ${ehCacheMulticastAddress}.
     */
    private final Map<String, String> systemProps = new HashMap<>(0);

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public EhcacheProperties() {
        this.crypto.setEnabled(false);
    }
}
