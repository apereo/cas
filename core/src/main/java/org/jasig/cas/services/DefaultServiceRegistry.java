/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultServiceRegistry implements ServiceRegistry,
    ServiceRegistryManager {
    
    final private Map services = new HashMap();

    public boolean serviceExists(final String serviceId) {
        return this.services.containsKey(serviceId);
    }

    public Collection getServices() {
        return this.services.values();
    }

    public void addService(final AuthenticatedService service) {
        this.services.put(service.getId(), service);
    }

    public boolean deleteService(final String serviceId) {
        return this.services.remove(serviceId) != null;
    }

    public AuthenticatedService getService(final String serviceId) {
        return (AuthenticatedService) this.services.get(serviceId);
    }
}
