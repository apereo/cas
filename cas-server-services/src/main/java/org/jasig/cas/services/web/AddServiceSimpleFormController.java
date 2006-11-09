/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.services.domain.MutableRegisteredServiceImpl;
import org.jasig.cas.services.domain.RegisteredService;
import org.jasig.cas.services.service.ServiceManager;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Implmentation of a SimpleFormController to register a new service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class AddServiceSimpleFormController extends SimpleFormController {

    private final ServiceManager serviceManager;

    public AddServiceSimpleFormController(final ServiceManager serviceManager) {
        Assert.notNull(serviceManager);
        this.serviceManager = serviceManager;
        this.setCommandClass(MutableRegisteredServiceImpl.class);
        this.setCommandName("registeredService");
    }

    // TODO attributes
    protected void initBinder(final HttpServletRequest request,
        final ServletRequestDataBinder binder) throws Exception {
        binder.setAllowedFields(new String[] {"id", "url", "name",
            "allowedToProxy", "enabled", "ssoParticipant"});
        binder.setRequiredFields(new String[] {"id", "url", "name"});
    }

    protected void doSubmitAction(final Object command) throws Exception {
        final RegisteredService service = (RegisteredService) command;

        this.serviceManager.addService(service);
    }
}
