/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.ServiceRegistry;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

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
    
    public ManageServicesController(final ServiceRegistry serviceRegistry) {
        Assert.notNull(serviceRegistry);
        this.serviceRegistry = serviceRegistry;
    }
    
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        final ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        
        modelAndView.addObject("services", this.serviceRegistry.getAllServices());
        modelAndView.addObject("pageTitle", VIEW_NAME);
        
        return modelAndView;
    }
}
