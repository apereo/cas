/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * TODO javadoc
 * Abstract SimpleFormController to handle adding/editing of RegisteredServices.
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public abstract class AbstractRegisteredServiceSimpleFormController extends
    SimpleFormController {
    
    /** Instance of ServiceRegistryManager */
    private final ServicesManager servicesManager;
    
    protected AbstractRegisteredServiceSimpleFormController(final ServicesManager servicesManager) {
        Assert.notNull(servicesManager);
        this.servicesManager = servicesManager;
    }
    
    protected final void initBinder(final HttpServletRequest request,
        final ServletRequestDataBinder binder) throws Exception {
        binder.setRequiredFields(new String[] {"description", "serviceId", "name", "allowedToProxy", "enabled", "ssoEnabled", "anonymousAccess"});
        binder.setDisallowedFields(new String[] {"id"});
    }
    
    protected final ServicesManager getServicesManager() {
        return this.servicesManager;
    }
    
    /**
     * Adds the service to the ServiceRegistry via the ServiceRegistryManager.
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    protected final ModelAndView onSubmit(final HttpServletRequest request, final HttpServletResponse response, final Object command, final BindException errors) throws Exception {
        final RegisteredService service = (RegisteredService) command;
        
        onSubmitInternal(service);
        
        final ModelAndView modelAndView = new ModelAndView(new RedirectView("/services/manage.html#" + service.getId(), true));
        modelAndView.addObject("action", "add");
        modelAndView.addObject("id", new Long(service.getId()));
        
        return modelAndView;
    }
    
    protected abstract void onSubmitInternal(RegisteredService registeredService);

    protected final Map referenceData(final HttpServletRequest request) throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final List<String> attributes = new ArrayList<String>();
        
        // TODO HACK
        attributes.add("test");
        attributes.add("test1");
        attributes.add("test2");
        attributes.add("test3");
        
        model.put("availableAttributes", attributes);
        model.put("pageTitle", getFormView());
        model.put("commandName", getCommandName());
        return model;
    }
}
