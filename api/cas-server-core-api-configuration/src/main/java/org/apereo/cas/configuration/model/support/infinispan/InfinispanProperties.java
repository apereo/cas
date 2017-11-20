package org.apereo.cas.configuration.model.support.infinispan;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * Encapsulates hazelcast properties exposed by CAS via properties file property source in a type-safe manner.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RequiresModule(name = "cas-server-support-infinispan-ticket-registry")
public class InfinispanProperties implements Serializable {

    private static final long serialVersionUID = 1974626726565626634L;
    /**
     * Path to the infinispan XML configuration file.
     */
    @RequiredProperty
    private Resource configLocation = new ClassPathResource("infinispan.xml");
    /**
     * Cache name to create and hold tickets in.
     */
    @RequiredProperty
    private String cacheName;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public InfinispanProperties() {
        this.crypto.setEnabled(false);
    }
    
    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }
    
    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
    
    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }
}
