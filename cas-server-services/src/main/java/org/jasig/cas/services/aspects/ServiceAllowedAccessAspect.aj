/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.aspects;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.domain.AccessDeniedException;
import org.jasig.cas.services.domain.RegisteredService;
import org.jasig.cas.services.service.ServiceManager;

import org.jasig.cas.CentralAuthenticationService;

/**
 * Aspect that determines whether a service is allowed to utilize CAS or not.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public aspect ServiceAllowedAccessAspect {

    /** Instance of Service Manager. */
    private ServiceManager serviceManager;

    before(Service service) : execution(* CentralAuthenticationService+.* (String, Service,..)) && args(String, service, ..) {
        final RegisteredService registeredService = this.serviceManager
            .getServiceByUrl(service.getId());

        if (registeredService == null || !registeredService.isEnabled()) {
            throw new AccessDeniedException("service: '" + service.getId()
                + "' denied access to CAS due to security restrictions.");
        }
    }

    public void setServiceManager(final ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
}
