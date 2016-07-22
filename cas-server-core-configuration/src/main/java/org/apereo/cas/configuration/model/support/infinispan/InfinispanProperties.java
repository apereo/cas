package org.apereo.cas.configuration.model.support.infinispan;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Encapsulates hazelcast properties exposed by CAS via properties file property source in a type-safe manner.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public class InfinispanProperties {
    
    private Resource configLocation = new ClassPathResource("infinispan.xml");
    private String cacheName;

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
