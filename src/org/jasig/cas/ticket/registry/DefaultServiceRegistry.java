/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.registry.ServiceRegistry;

/**
 * Default Implementation of the {@link ServiceRegistry}
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.registry.ServiceRegistry
 */
public class DefaultServiceRegistry implements ServiceRegistry {

    protected final Log log = LogFactory.getLog(getClass());

    final private List services;

    public DefaultServiceRegistry() {
        this.services = new ArrayList();
    }

    public DefaultServiceRegistry(final List services) {
        this.services = new ArrayList();
        this.services.addAll(services);
    }

    public void addService(final String service) {
        this.services.add(service);
    }

    public boolean serviceExists(final String service) {
        log.debug("Attempting to determine if service [" + service + "] exists.");

        if (this.services.isEmpty()) {
            log.debug("Services List is empty.  Assuming service registry is not in use.");
            return true;
        }

        if (service == null)
            return false;

        for (Iterator iter = this.services.iterator(); iter.hasNext();) {
            String validService = (String)iter.next();

            if (service.startsWith(validService)) {
                log.debug("Service [" + service + "] found in registry.");
                return true;
            }
        }
        log.debug("Unable to find service [" + service + "] in registry.");
        return false;
    }

    public void deleteService(final String service) {
        this.services.remove(service);
    }

    public List getServices() {
        return Collections.unmodifiableList(this.services);
    }
}