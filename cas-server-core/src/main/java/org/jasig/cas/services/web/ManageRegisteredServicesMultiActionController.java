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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.math.NumberUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * MultiActionController to handle the deletion of RegisteredServices as well as
 * displaying them on the Manage Services page.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ManageRegisteredServicesMultiActionController extends MultiActionController {

    /** View name for the Manage Services View. */
    private static final String VIEW_NAME = "manageServiceView";

    /** Instance of ServicesManager. */
    @NotNull
    private final ServicesManager servicesManager;

    @NotNull
    private final String defaultServiceUrl;
    
    /**
     * Constructor that takes the required {@link ServicesManager}.
     * 
     * @param servicesManager the Services Manager that manages the
     * RegisteredServices.
     * @param defaultServiceUrl the service management tool's url.
     */
    public ManageRegisteredServicesMultiActionController(
        final ServicesManager servicesManager, final String defaultServiceUrl) {
        super();
        this.servicesManager = servicesManager;
        this.defaultServiceUrl = defaultServiceUrl;
    }

    /**
     * Method to delete the RegisteredService by its ID.
     * 
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @return the Model and View to go to after the service is deleted.
     */
    public ModelAndView deleteRegisteredService(
        final HttpServletRequest request, final HttpServletResponse response) {
        final String id = request.getParameter("id");
        final long idAsLong = Long.parseLong(id);

        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
            "manage.html", true), "status", "deleted");

        final RegisteredService r = this.servicesManager.delete(idAsLong);

        modelAndView.addObject("serviceName", r != null
            ? r.getName() : "");

        return modelAndView;
    }

    /**
     * Method to show the RegisteredServices.
     * 
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @return the Model and View to go to after the services are loaded.
     */
    public ModelAndView manage(final HttpServletRequest request,
        final HttpServletResponse response) {
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
    * 
    * @param request The request object that is expected to contain the <code>id</code> and <code>evaluationOrder</code>
    *        as parameters.
    * @param response The response object.
    *       
    * @returns {@link ModelAndView} object that redirects to a <code>jsonView</code>. The model will contain a
    *          a parameter <code>error</code> whose value should describe the error occurred if the update is unsuccesful.
    *          There will also be a <code>successful</code> boolean parameter that indicates whether or not the update 
    *          was successful.
    *          
    * @throws IllegalArgumentException If either of the <code>id</code> or <code>evaluationOrder</code> are invalid
    *         or if the service cannot be located for that id by the active implementation of the {@link ServicesManager}.  
    */
    public ModelAndView updateRegisteredServiceEvaluationOrder(final HttpServletRequest request, final HttpServletResponse response) {
        long id = Long.parseLong(request.getParameter("id"));
        int evaluationOrder = Integer.parseInt(request.getParameter("evaluationOrder"));
        
        final RegisteredService svc = this.servicesManager.findServiceBy(id);
        if (svc == null)
            throw new IllegalArgumentException("Service id " + id + " cannot be found.");
        
        svc.setEvaluationOrder(evaluationOrder);
        this.servicesManager.save(svc);
        
        return new ModelAndView("jsonView");
    }
}
