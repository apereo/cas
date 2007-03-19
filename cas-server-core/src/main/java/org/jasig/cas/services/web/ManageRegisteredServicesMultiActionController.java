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
import org.springframework.beans.support.PropertyComparator;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;


/**
 * 
 * // TODO javadoc
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1 
 *
 */
public class ManageRegisteredServicesMultiActionController extends
    MultiActionController {
    
    private static final String VIEW_NAME = "manageServiceView";
    
    private ServicesManager servicesManager;
    
    private final PropertyComparator propertyComparator = new PropertyComparator(
        "name", false, true);
    
    public ManageRegisteredServicesMultiActionController(final ServicesManager servicesManager) {
        Assert.notNull(servicesManager, "servicesManager cannot be null");
        this.servicesManager = servicesManager;
    }

    
    public ModelAndView deleteRegisteredService(final HttpServletRequest request, final HttpServletResponse response) {
        final String id = request.getParameter("id");
        final long idAsLong = Long.parseLong(id);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(idAsLong);

        final ModelAndView modelAndView = new ModelAndView(new RedirectView("/services/manage.html", true), "status", "deleted");
        modelAndView.addObject("serviceName", registeredService != null ? registeredService.getName() : "");
        
        this.servicesManager.delete(this.servicesManager.findServiceBy(idAsLong));
        
        return modelAndView;
    }
    
    public ModelAndView enableRegistryService(final HttpServletRequest request, final HttpServletResponse response) {
// XXX fix!
        // this.serviceRegistry.setEnabled(true);
        return new ModelAndView(new RedirectView("/services/manage.html", true), "status", "enabled");
    }
    
    public ModelAndView disableRegistryService(final HttpServletRequest request, final HttpServletResponse response) {
// XXX fix!        this.serviceRegistry.setEnabled(false);
        return new ModelAndView(new RedirectView("/services/manage.html", true), "status", "disabled");
    }
 
    public ModelAndView manage(final HttpServletRequest request, final HttpServletResponse response) {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final List<RegisteredService> services = new ArrayList<RegisteredService>(
            this.servicesManager.getAllServices());
        PropertyComparator.sort(services, this.propertyComparator
            .getSortDefinition());

        model.put("services", services);
        model.put("pageTitle", VIEW_NAME);
        /* this.serviceRegistry.isEnabled() */
        model.put("currentRegistryStatus",  true ? Boolean.TRUE : Boolean.FALSE);

        return new ModelAndView(VIEW_NAME, model);
    }
}
