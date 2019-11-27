package org.apereo.cas.configuration.model.support.ehcache;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
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
public class Ehcache3Properties implements Serializable {

    /**
     * EhCache autocreate mode for creating config in Terracotta cluster.
     * Creates config as specified if not present.
     */
    public static final String CONNECTION_MODE_AUTOCREATE = "AUTOCREATE";

    /**
     * EhCache configless mode  for creating config in Terracotta cluster.
     * Requires config to already be defined on Terracotta cluster.
     */
    public static final String CONNECTION_MODE_CONFIGLESS = "CONFIGLESS";

    private static final long serialVersionUID = 7772510035918976450L;

    /**
     * The name of the cache manager instance.
     */
    @RequiredProperty
    private String cacheManagerName = "ticketRegistryCacheManager";

    /**
     * Builder that sets the maximum objects to be held in memory (0 = no limit).
     */
    private int maxElementsInMemory = 10_000;

    /**
     * Size of disk cache.
     */
    private int maxSizeOnDisk = 200;

    /**
     * Disk cache size units as defined by EhCache. (e.g. MB, GB)
     */
    private String maxSizeOnDiskUnits = "MB";

    /**
     * Per cache size of disk cache.
     */
    private int perCacheSizeOnDisk = 20;

    /**
     * Per cache disk cache size units as defined by EhCache. (e.g. MB, GB)
     */
    private String perCacheSizeOnDiskUnits = "MB";


    /**
     * Size of off heap cache.
     */
    private int maxSizeOffHeap = 100;

    /**
     * Off heap cache size units as defined by EhCache. (e.g. MB, GB)
     */
    private String maxSizeOffHeapUnits = "MB";

    /**
     * Sets whether elements are eternal.
     * If eternal, timeouts are ignored and the element is never expired. False by default.
     * Functionality brought over from Ehcache 2, document use case.
     */
    private boolean eternal;

    /**
     * URI in format something like "terracotta://localhost/my-application".
     * Default port for terracotta (9410) is ussed if not specified in URI.
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
    private int resourcePoolSize = 15;

    /**
     * Units for size of resource pool on terracotta cluster.
     */
    private String resourcePoolUnits = "MB";

    /**
     * Sets the connection mode to the terracotta cluster.
     * Acceptable values are:
     * <ul>
     * <li>AUTOCREATE: In auto-create mode if no cluster tier manager exists then one is created with the supplied configuration.
     * If it exists and its configuration matches the supplied configuration then a connection is established.
     * If the supplied configuration does not match then the cache manager will fail to initialize.</li>
     * <li>CONFIGLESS: In config-less mode if a cluster tier manager exists then a connection is established without regard to its configuration.
     * If it does not exist then the cache manager will fail to initialize</li>
     * </ul>
     */
    private String connectionMode = CONNECTION_MODE_AUTOCREATE;


    /**
     * Root directory to store data if not using terracotta cluster.
     */
    private String rootDirectory = "/tmp/cas/ehcache3";


    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public Ehcache3Properties() {
        this.crypto.setEnabled(false);
    }

}

