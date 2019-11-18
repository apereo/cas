package org.apereo.cas.configuration.model.support.ehcache;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link Ehcache3Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-ehcache3-ticket-registry")
@Getter
@Setter
public class Ehcache3Properties implements Serializable {

    private static final long serialVersionUID = 7772510035918976450L;

    public static final String CONNECTION_MODE_AUTOCREATE = "AUTOCREATE";
    public static final String CONNECTION_MODE_CONFIGLESS = "CONFIGLESS";

    /**
     * The name of the cache manager instance.
     */
    @RequiredProperty
    private String cacheManagerName = "ticketRegistryCacheManager";

    /**
     * Builder that sets the maximum objects to be held in memory (0 = no limit).
     */
    private int maxElementsInMemory = 10_000;

    private int maxSizeOnDisk = 100;

    private String maxSizeOnDiskUnits = "MB";

    private int maxSizeOffHeap = 100;

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


    private String defaultServerResource = "cas-ticket-registry";

    private String resourcePoolName = "cas-ticket-registry-resource-pool";

    private int resourcePoolSize = 100;

    private String resourcePoolUnits = "MB";

    /**
     * Sets the connection mode to the terracotta cluster.
     * Acceptable values are:
     * <ul>
     * <li>AUTOCREATE: In auto-create mode if no cluster tier manager exists then one is created with the supplied configuration.
     * If it exists and its configuration matches the supplied configuration then a connection is established. If the supplied configuration does not match then the cache manager will fail to initialize.</li>
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

