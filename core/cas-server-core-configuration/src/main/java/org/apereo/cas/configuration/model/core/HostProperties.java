package org.apereo.cas.configuration.model.core;

import java.io.Serializable;

/**
 * Configuration properties class for host.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class HostProperties implements Serializable {
    private static final long serialVersionUID = 8624916460241033347L;
    /**
     * Name of the networking host configured to run CAS server.
     */
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
