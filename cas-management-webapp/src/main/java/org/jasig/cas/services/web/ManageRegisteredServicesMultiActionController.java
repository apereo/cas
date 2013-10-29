/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private static final String VIEW_NAME = "manageServiceView";

    /** Instance of ServicesManager. */
    @NotNull
    @Resource(name="servicesManager")
    private ServicesManager servicesManager;

    @NotNull
    @Value("${cas-management.securityContext.serviceProperties.service}")
    private String defaultServiceUrl;

    public ManageRegisteredServicesMultiActionController() {}
    
    /**
     * Method to delete the RegisteredService by its ID.
     * @return the Model and View to go to after the service is deleted.
     */
    @RequestMapping("deleteRegisteredService.html")
    public ModelAndView deleteRegisteredService(
            @RequestParam("id") final long idAsLong) {
        
        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
                "manage.html", true), "status", "deleted");

        final RegisteredService r = this.servicesManager.delete(idAsLong);

        modelAndView.addObject("serviceName", r != null
                ? r.getName() : "");

        return modelAndView;
    }

    /**
     * Method to show the RegisteredServices.
     * @return the Model and View to go to after the services are loaded.
     */
    @RequestMapping("manage.html")
    public ModelAndView manage() {
        final Map<String, Object> model = new HashMap<String, Object>();

        final List<RegisteredService> services = new ArrayList<RegisteredService>(this.servicesManager.getAllServices());

        model.put("services", services);
        model.put("pageTitle", VIEW_NAME);
        model.put("defaultServiceUrl", this.defaultServiceUrl);

        return new ModelAndView(VIEW_NAME, model);
    }

    /**
     * Updates the {@link RegisteredService#getEvaluationOrder()}. Expects an <code>id</code> parameter to indicate
     * the {@link RegisteredService#getId()} and the new <code>evaluationOrder</code> integer parameter from the request.
     * as parameters.
     * @returns {@link ModelAndView} object that redirects to a <code>jsonView</code>. The model will contain a
     * a parameter <code>error</code> whose value should describe the error occurred if the update is unsuccesful.
     * There will also be a <code>successful</code> boolean parameter that indicates whether or not the update
     * was successful.
     * @throws IllegalArgumentException If either of the <code>id</code> or <code>evaluationOrder</code> are invalid
     * or if the service cannot be located for that id by the active implementation of the {@link ServicesManager}.
     * @return a {@link ModelAndView} object back to the <code>jsonView</code>
     */
    @RequestMapping("updateRegisteredServiceEvaluationOrder.html")
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
