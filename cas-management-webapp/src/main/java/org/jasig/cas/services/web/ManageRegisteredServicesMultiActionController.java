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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * MultiActionController to handle the deletion of RegisteredServices as well as
 * displaying them on the Manage Services page.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller
public final class ManageRegisteredServicesMultiActionController {

    /** View name for the Manage Services View. */
    public static final String VIEW_NAME = "manageServiceView";

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
     * @return the view name.
     */
    @RequestMapping(value="loggedOut.html", method={RequestMethod.GET})
    public String logoutView() {
        return "logout";
    }


    /**
     * Method to delete the RegisteredService by its ID.
     *
     * @param idAsLong the id
     * @return the Model and View to go to after the service is deleted.
     */
    @RequestMapping(value="deleteRegisteredService.html", method={RequestMethod.GET})
    public ModelAndView deleteRegisteredService(
            @RequestParam("id") final long idAsLong) {
        
        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
                "manage.html", true), "status", "deleted");

        final RegisteredService r = this.servicesManager.delete(idAsLong);
        modelAndView.addObject("serviceName", r != null ? r.getName() : "");

        return modelAndView;
    }

    /**
     * Method to show the RegisteredServices.
     * @return the Model and View to go to after the services are loaded.
     */
    @RequestMapping(value="manage.html", method={RequestMethod.GET})
    public ModelAndView manage() {
        final Map<String, Object> model = new HashMap<>();

        final List<RegisteredService> services = new ArrayList<>(this.servicesManager.getAllServices());

        model.put("services", services);
        model.put("pageTitle", VIEW_NAME);
        model.put("defaultServiceUrl", this.defaultServiceUrl);

        return new ModelAndView(VIEW_NAME, model);
    }

    /**
     * Updates the {@link RegisteredService#getEvaluationOrder()}. Expects an <code>id</code> parameter to indicate
     * the {@link RegisteredService#getId()} and the new <code>evaluationOrder</code> integer parameter from the request.
     * as parameters.
     *
     * @param id the id
     * @param evaluationOrder the evaluation order
     * @return {@link ModelAndView} object that redirects to a <code>jsonView</code>. The model will contain a
     * a parameter <code>error</code> whose value should describe the error occurred if the update is unsuccessful.
     * There will also be a <code>successful</code> boolean parameter that indicates whether or not the update
     * was successful.
     */
    @RequestMapping(value="updateRegisteredServiceEvaluationOrder.html", method={RequestMethod.GET})
    public ModelAndView updateRegisteredServiceEvaluationOrder(@RequestParam("id") final long id,
            @RequestParam("evaluationOrder") final int evaluationOrder) {
        final RegisteredService svc = this.servicesManager.findServiceBy(id);
        if (svc == null) {
            throw new IllegalArgumentException("Service id " + id + " cannot be found.");
        }

        svc.setEvaluationOrder(evaluationOrder);
        this.servicesManager.save(svc);

        return new ModelAndView("jsonView");
    }
}
