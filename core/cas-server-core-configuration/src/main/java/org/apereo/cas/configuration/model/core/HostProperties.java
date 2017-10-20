package org.apereo.cas.configuration.model.core;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * Configuration properties class for host.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core", automated = true)
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
