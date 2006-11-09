/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.domain.RegisteredService;
import org.jasig.cas.services.service.ServiceManager;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Controller that handles the display and deletion of services.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class DisplayAndDeleteMultiActionController extends
    MultiActionController {

    /**
     * Instance of Service Manager.
     */
    private final ServiceManager serviceManager;

    public DisplayAndDeleteMultiActionController(
        final ServiceManager serviceManager) {
        Assert.notNull(this.serviceManager, "serviceManager cannot be null.");
        this.serviceManager = serviceManager;
    }

    public ModelAndView displayAllServices(final HttpServletRequest request,
        final HttpServletResponse response) {
        final RegisteredService[] services = this.serviceManager
            .getAllServices();

        return new ModelAndView("displayAllServicesView", "services", services);
    }

    public ModelAndView deleteService(final HttpServletRequest request,
        final HttpServletResponse response) {
        final String id = request.getParameter("id");

        if (id == null) {
            return new ModelAndView("redirect:displayAllServices.html",
                "delete", "no");
        }

        this.serviceManager.deleteService(id);

        return new ModelAndView("redirect:displayAllServices.html", "delete",
            "yes");
    }
}
