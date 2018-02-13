package org.apereo.cas.configuration.support;

import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link SpringResourceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SpringResourceProperties implements Serializable {
    private static final long serialVersionUID = 4142130961445546358L;
    /**
     * The location of service definitions. Resources can be URLS, or
     * files found either on the classpath or outside somewhere
     * in the file system.
     */
    @RequiredProperty
    private transient Resource location;

    public Resource getLocation() {
        return location;
    }

    public void setLocation(final Resource location) {
        this.location = location;
    }
}
