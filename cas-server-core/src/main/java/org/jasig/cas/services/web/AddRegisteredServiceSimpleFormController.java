/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class AddRegisteredServiceSimpleFormController extends
    AbstractRegisteredServiceSimpleFormController {
    
    /**
     * Public constructor that takes a {@link ServiceRegistryManager}
     * 
     * @param servicesManager the ServiceRegistryManager
     */
    public AddRegisteredServiceSimpleFormController(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    protected void onSubmitInternal(final RegisteredService registeredService) {
        getServicesManager().save(registeredService);
    }
}
