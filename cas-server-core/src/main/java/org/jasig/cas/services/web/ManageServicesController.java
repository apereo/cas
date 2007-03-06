/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class ManageServicesController extends AbstractController {

    private static final String VIEW_NAME = "manageServiceView"; 
    
    private final ServiceRegistry serviceRegistry;
    
    private final ServiceRegistryManager serviceRegistryManager;
    
    public ManageServicesController(final ServiceRegistry serviceRegistry, final ServiceRegistryManager serviceRegistryManager) {
        Assert.notNull(serviceRegistry, "serviceRegistry cannot be null");
        Assert.notNull(serviceRegistryManager, "serviceRegistryManager cannot be null");
        this.serviceRegistry = serviceRegistry;
        this.serviceRegistryManager = serviceRegistryManager;
    }
    
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        final ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        final String action = request.getParameter("action");
        final String c = request.getParameter("confirm");
        final boolean confirm = Boolean.parseBoolean(c);
        final long id = Long.parseLong(request.getParameter("id") != null ? request.getParameter("id") : "-1");
        
        if (confirm) {
            this.serviceRegistryManager.deleteService(id);
        }
        
        if (action != null && c != null) {
            return new ModelAndView(new RedirectView("/services/manage.html", true));
        }
        
        modelAndView.addObject("services", this.serviceRegistry.getAllServices());
        modelAndView.addObject("pageTitle", VIEW_NAME);
        
        return modelAndView;
    }
}
