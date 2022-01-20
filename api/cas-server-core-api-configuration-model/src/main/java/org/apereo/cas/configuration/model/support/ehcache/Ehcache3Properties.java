package org.apereo.cas.configuration.model.support.ehcache;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("Ehcache3Properties")
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
     * When set to false then storage timeouts will be set based on the the individual caches timeouts.
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
     * Root directory to store data if not using terracotta cluster.
     */
    private String rootDirectory = "/tmp/cas/ehcache3";

    /**
     * Persist data on disk when jvm is shut down if not using terracotta cluster.
     * The caches will survive a restart.
     */
    private boolean persistOnDisk = true;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto =
        new EncryptionRandomizedSigningJwtCryptographyProperties();

    /**
     * Terracotta settings to handle clustered tickets.
     */
    @NestedConfigurationProperty
    private Ehcache3TerracottaProperties terracotta = new Ehcache3TerracottaProperties();

    public Ehcache3Properties() {
        this.crypto.setEnabled(false);
    }
}

