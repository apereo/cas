/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ManageServicesController extends AbstractController {

    private static final String VIEW_NAME = "manageServiceView";

    private final ServiceRegistry serviceRegistry;

    private final ServiceRegistryManager serviceRegistryManager;

    private PropertyComparator propertyComparator = new PropertyComparator(
        "name", false, true);

    public ManageServicesController(final ServiceRegistry serviceRegistry,
        final ServiceRegistryManager serviceRegistryManager) {
        Assert.notNull(serviceRegistry, "serviceRegistry cannot be null");
        Assert.notNull(serviceRegistryManager,
            "serviceRegistryManager cannot be null");
        this.serviceRegistry = serviceRegistry;
        this.serviceRegistryManager = serviceRegistryManager;
    }

    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String action = request.getParameter("action");
        final String c = request.getParameter("confirm");
        final boolean confirm = Boolean.parseBoolean(c);
        final long id = Long.parseLong(request.getParameter("id") != null
            ? request.getParameter("id") : "-1");
      
        final ModelAndView enabled = checkEnableAndProcess(action, confirm);
        final ModelAndView deleted = checkDeleteAndProcess(action, confirm, c, id);
        
        return enabled != null ? enabled : deleted != null ? deleted : getDefaultModelAndView();
    }
    
    private ModelAndView getDefaultModelAndView() {
        final ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        final List<RegisteredService> services = new ArrayList<RegisteredService>(
            this.serviceRegistry.getAllServices());
        PropertyComparator.sort(services, this.propertyComparator
            .getSortDefinition());

        modelAndView.addObject("services", services);
        modelAndView.addObject("pageTitle", VIEW_NAME);
        modelAndView.addObject("currentRegistryStatus", this.serviceRegistry.isEnabled() ? Boolean.TRUE :Boolean.FALSE);
        
        return modelAndView;
    }
    
    private ModelAndView checkDeleteAndProcess(final String action, final boolean confirm, final String c, final long id) {
        if (!"delete".equals(action) || c == null) {
            return null;
        }
        
        if (!confirm) {
            return new ModelAndView(new RedirectView("/services/manage.html",
                true));
        }
        
        if (!this.serviceRegistryManager.deleteService(id)) {
            return new ModelAndView(new RedirectView("/services/manage.html?status=notdeleted",
                true));
        }
        
        return new ModelAndView(new RedirectView("/services/manage.html?status=deleted",
            true));
    }
    
    private ModelAndView checkEnableAndProcess(final String action, final boolean confirm) {
        if (!"enable".equals(action)) {
            return null;
        }
        
        this.serviceRegistry.setEnabled(confirm);
        
        if (confirm) {
            return new ModelAndView(new RedirectView("/services/manage.html?status=enabled",
                true));
        }
        
        return new ModelAndView(new RedirectView("/services/manage.html?status=disabled",
            true));
    }
}
