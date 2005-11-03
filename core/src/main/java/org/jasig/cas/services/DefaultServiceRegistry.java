/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of the Service Registry. It stores all services in a
 * HashMap. Implementation is basically a type-safe wrapper around the HashMap.
 * <p>
 * The Service Registry is not thread-safe so any class using the
 * ServiceRegistry needs to ensure they synchronize on the registry.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultServiceRegistry implements ServiceRegistry,
    ServiceRegistryManager {

    /** Logging instance. */
    private final Log log = LogFactory.getLog(this.getClass());

    /** The map containing the services. */
    private final Map services = new HashMap();

    public boolean serviceExists(final String serviceId) {
        return this.services.containsKey(serviceId);
    }

    public Collection getServices() {
        return this.services.values();
    }

    public void addService(final RegisteredService service) {
        if (log.isDebugEnabled()) {
            log.debug("Adding service [" + service.getId()
                + "] to serviceRegistry");
        }
        this.services.put(service.getId(), service);
    }

    public boolean deleteService(final String serviceId) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting service[" + serviceId
                + "] from Service Registry.");
        }
        return this.services.remove(serviceId) != null;
    }

    public RegisteredService getService(final String serviceId) {
        RegisteredService authenticatedService = (RegisteredService) this.services
            .get(serviceId);
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve service [" + serviceId
                + "] from Service Registry");
        }
        if (authenticatedService != null) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved service [" + serviceId
                    + "] from Service Registry.");
            }
        }
        return authenticatedService;
    }

    public void clear() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing all entries from Service Registry");
        }
        this.services.clear();
    }
}
