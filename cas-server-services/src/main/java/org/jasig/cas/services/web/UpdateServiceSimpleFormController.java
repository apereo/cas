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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class UpdateServiceSimpleFormController extends SimpleFormController {

    private final ServiceManager serviceManager;
    
    public UpdateServiceSimpleFormController(final ServiceManager serviceManager) {
        Assert.notNull(serviceManager);
        this.serviceManager = serviceManager;
        this.setCommandClass(MutableRegisteredServiceImpl.class);
        this.setCommandName("registeredService");
    }

    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) throws Exception {
        binder.setAllowedFields(new String[] {"url", "allowedToProxy", "enabled", "ssoParticipant"});
        binder.setRequiredFields(new String[] {"url"});
    }

    protected void doSubmitAction(final Object command) throws Exception {
        final RegisteredService service = (RegisteredService) command;
        
        this.serviceManager.updateService(service);
    }

    protected Object formBackingObject(final HttpServletRequest request) throws Exception {
        final String id = request.getParameter("id");
        
        if (!StringUtils.hasText(id)) {
            throw new IllegalStateException("id request parameter not found.");
        }
        
        final RegisteredService registeredService = this.serviceManager.getServiceById(id);
        
        if (registeredService == null) {
            throw new IllegalStateException("service matching id: " + id + " not found.");
        }
        
        return registeredService;
    }
}
