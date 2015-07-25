/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MultiActionController to handle the deletion of RegisteredServices as well as
 * displaying them on the Manage Services page.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller
public final class ManageRegisteredServicesMultiActionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageRegisteredServicesMultiActionController.class);

    /** Instance of ServicesManager. */
    @NotNull
    private final ServicesManager servicesManager;

    @NotNull
    private final String defaultServiceUrl;

    /**
     * Instantiates a new manage registered services multi action controller.
     *
     * @param servicesManager the services manager
     * @param defaultServiceUrl the default service url
     */
    @Autowired
    public ManageRegisteredServicesMultiActionController(final ServicesManager servicesManager,
            @Value("${cas-management.securityContext.serviceProperties.service}") final String defaultServiceUrl) {
        this.servicesManager = servicesManager;
        this.defaultServiceUrl = defaultServiceUrl;
    }

    /**
     * Ensure default service exists.
     */
    private void ensureDefaultServiceExists() {
        final Collection<RegisteredService> c = this.servicesManager.getAllServices();
        if (c.isEmpty()) {
            throw new IllegalStateException("Services cannot be empty");
        }

        if (!this.servicesManager.matchesExistingService(
                new SimpleWebApplicationServiceImpl(this.defaultServiceUrl))) {
            final RegexRegisteredService svc = new RegexRegisteredService();
            svc.setServiceId(defaultServiceUrl);
            svc.setName("Services Management Web Application");
            this.servicesManager.save(svc);
        }
    }
    /**
     * Authorization failure handling. Simply returns the view name.
     *
     * @return the view name.
     */
    @RequestMapping(value="authorizationFailure.html", method={RequestMethod.GET})
    public String authorizationFailureView() {
        return "authorizationFailure";
    }

    /**
     * Logout handling. Simply returns the view name.
     *
     * @param request the request
     * @param session the session
     * @return the view name.
     */
    @RequestMapping(value="logout.html", method={RequestMethod.GET})
    public String logoutView(final HttpServletRequest request, final HttpSession session) {
        LOGGER.debug("Invalidating application session...");
        session.invalidate();
        return "logout";
    }

    /**
     * Method to delete the RegisteredService by its ID. Will make sure
     * the default service that is the management app itself cannot be deleted
     * or the user will be locked out.
     *
     * @param idAsLong the id
     * @return the Model and View to go to after the service is deleted.
     */
    @RequestMapping(value="deleteRegisteredService.html", method={RequestMethod.POST})
    public ModelAndView deleteRegisteredService(@RequestParam("id") final long idAsLong) {
        final RegisteredService defaultRegSvc = this.servicesManager.findServiceBy(
                new SimpleWebApplicationServiceImpl(this.defaultServiceUrl));
        if (defaultRegSvc.getId() == idAsLong) {
            throw new IllegalArgumentException("Default service" + idAsLong + " cannot be deleted.");
        }

        final RegisteredService r = this.servicesManager.delete(idAsLong);
        if (r == null) {
            throw new IllegalArgumentException("Service id " + idAsLong + " cannot be found.");
        }
        final ModelAndView modelAndView = new ModelAndView(new RedirectView("manage.html", true));
        modelAndView.addObject("serviceName", r.getName());
        return modelAndView;
    }

    /**
     * Method to show the RegisteredServices.
     * @return the Model and View to go to after the services are loaded.
     */
    @RequestMapping(value="manage.html", method={RequestMethod.GET})
    public ModelAndView manage() {
        ensureDefaultServiceExists();
        final Map<String, Object> model = new HashMap<>();
        final List<RegisteredService> services = new ArrayList<>(this.servicesManager.getAllServices());
        model.put("services", services);
        return new ModelAndView("manage", model);
    }

    /**
     * Updates the {@link RegisteredService#getEvaluationOrder()}.
     *
     * @param id the service ids, whose order also determines the service evaluation order
     * @return {@link ModelAndView} object that redirects to a <code>jsonView</code>.
     */
    @RequestMapping(value="updateRegisteredServiceEvaluationOrder.html", method={RequestMethod.POST})
    public ModelAndView updateRegisteredServiceEvaluationOrder(@RequestParam("id") final long... id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("No service id was received. Re-examine the request");
        }
        for (int i = 0; i < id.length; i++) {
            final long svcId = id[i];
            final RegisteredService svc = this.servicesManager.findServiceBy(svcId);
            if (svc == null) {
                throw new IllegalArgumentException("Service id " + svcId + " cannot be found.");
            }
            svc.setEvaluationOrder(i);
            this.servicesManager.save(svc);
        }
        return new ModelAndView("jsonView");
    }
}
