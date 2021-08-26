package org.apereo.cas.configuration.model.support.ehcache;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link Ehcache3Properties}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-ehcache3-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class Ehcache3Properties implements Serializable {

    private static final long serialVersionUID = 7772510035918976450L;

    /**
     * Enabled allows this registry to be disabled on startup (so registry choice can be made at runtime).
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * Builder that sets the maximum objects to be held in memory (0 = no limit).
     */
    private int maxElementsInMemory = 10_000;

    /**
     * Per cache size of disk cache.
     */
    private String perCacheSizeOnDisk = "20MB";

    /**
     * Sets whether elements are eternal.
     * If eternal, timeouts are ignored and the element is never expired. False by default.
     * Functionality brought over from Ehcache 2, document use case.
     */
    private boolean eternal;

    /**
     * Sets whether statistics are enabled for all caches.
     */
    private boolean enableStatistics = true;

    /**
     * Sets whether JMX management beans are enabled for all caches.
     */
    private boolean enableManagement = true;

    /**
     * URI in format something like "terracotta://host1.company.org:9410,host2.company.org:9410/cas-application".
     * Default port for terracotta (9410) is used if not specified in URI.
     */
    private String terracottaClusterUri;

    /**
     * Name of default server resource on Terracotta cluster.
     */
    private String defaultServerResource = "main";

    /**
     * Name of resource pool to use on Terracotta cluster.
     */
    private String resourcePoolName = "cas-ticket-pool";

    /**
     * Size of resource pool on terracotta cluster.
     */
    private String resourcePoolSize = "15MB";

    /**
     * Root directory to store data if not using terracotta cluster.
     */
    private String rootDirectory = "/tmp/cas/ehcache3";

    /**
     * Persist data on disk when jvm is shut down if not using terracotta cluster.
     */
    private boolean persistOnDisk = true;

    /**
     * Timeout when reading or writing to/from Terracotta cluster.
     */
    private long clusterReadWriteTimeout = 5L;

    /**
     * Timeout when connecting to Terracotta cluster.
     */
    private long clusterConnectionTimeout = 150L;

    /**
     * Cluster consistency may be STRONG or EVENTUAL.
     */
    private Consistency clusteredCacheConsistency = Consistency.STRONG;

    /**
     * Enumeration of the different consistency levels supported in clustered caches.
     */
    public enum Consistency {

        /**
         * Indicates that the visibility of mutative operations is not guaranteed on operation completion.
         */
        EVENTUAL,
        /**
         * Indicates that the visibility of mutative operations is guaranteed on operation completion.
         */
        STRONG

    }

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public Ehcache3Properties() {
        this.crypto.setEnabled(false);
    }
}

