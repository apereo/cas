package org.apereo.cas.configuration.support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link BaseResourceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class BaseResourceProperties implements Serializable {
    /**
     * The location of service definitions. Resources can be URLS, or
     * files found either on the classpath or outside somewhere
     * in the file system.
     */
    private Resource location = new ClassPathResource("services");

    public Resource getLocation() {
        return location;
    }

    public void setLocation(final Resource location) {
        this.location = location;
    }
}
