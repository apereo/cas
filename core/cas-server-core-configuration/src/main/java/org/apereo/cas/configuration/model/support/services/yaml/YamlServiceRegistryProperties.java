package org.apereo.cas.configuration.model.support.services.yaml;

import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link YamlServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class YamlServiceRegistryProperties implements Serializable {
    /**
     * The resource location. Resources can be URLS, or
     * files found either on the classpath or outside somewhere
     * in the file system.
     */
    private Resource location;

    public Resource getLocation() {
        return location;
    }

    public void setLocation(final Resource location) {
        this.location = location;
    }
}
