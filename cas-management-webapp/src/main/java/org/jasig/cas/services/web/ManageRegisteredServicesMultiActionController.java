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

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;
import org.jasig.cas.web.view.JsonViewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public final class ManageRegisteredServicesMultiActionController extends AbstractManagementController {
    @NotNull
    private final Service defaultService;

    /**
     * Instantiates a new manage registered services multi action controller.
     *
     * @param servicesManager the services manager
     * @param defaultServiceUrl the default service url
     */
    @Autowired
    public ManageRegisteredServicesMultiActionController(
             final ReloadableServicesManager servicesManager,
            @Value("${cas-management.securityContext.serviceProperties.service}") final String defaultServiceUrl) {
        super(servicesManager);
        this.defaultService = new SimpleWebApplicationServiceImpl(defaultServiceUrl);
    }

    /**
     * Ensure default service exists.
     */
    private void ensureDefaultServiceExists() {
        this.servicesManager.reload();
        final Collection<RegisteredService> c = this.servicesManager.getAllServices();
        if (c == null) {
            throw new IllegalStateException("Services cannot be empty");
        }

        if (!this.servicesManager.matchesExistingService(defaultService)) {
            final RegexRegisteredService svc = new RegexRegisteredService();
            svc.setServiceId('^' + defaultService.getId());
            svc.setName("Services Management Web Application");
            svc.setDescription(svc.getName());
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
        logger.debug("Invalidating application session...");
        session.invalidate();
        return "logout";
    }

    /**
     * Method to delete the RegisteredService by its ID. Will make sure
     * the default service that is the management app itself cannot be deleted
     * or the user will be locked out.
     *
     * @param idAsLong the id
     * @param response the response
     */
    @RequestMapping(value="deleteRegisteredService.html", method={RequestMethod.POST})
    public void deleteRegisteredService(@RequestParam("id") final long idAsLong,
                                        final HttpServletResponse response) {
        final RegisteredService svc = this.servicesManager.findServiceBy(this.defaultService);
        if (svc == null || svc.getId() == idAsLong) {
            throw new IllegalArgumentException("The default service " + defaultService.getId() + " cannot be deleted. "
                                       + "The definition is required for accessing the application.");
        }

        final RegisteredService r = this.servicesManager.delete(idAsLong);
        if (r == null) {
            throw new IllegalArgumentException("Service id " + idAsLong + " cannot be found.");
        }
        final Map<String, Object> model = new HashMap<>();
        model.put("serviceName", r.getName());
        model.put("status", HttpServletResponse.SC_OK);
        JsonViewUtils.render(model, response);
    }

    /**
     * Method to show the RegisteredServices.
     * @param response the response
     * @return the Model and View to go to after the services are loaded.
     */
    @RequestMapping(value="manage.html", method={RequestMethod.GET})
    public ModelAndView manage(final HttpServletResponse response) {
        ensureDefaultServiceExists();
        final Map<String, Object> model = new HashMap<>();
        model.put("defaultServiceUrl", this.defaultService.getId());
        model.put("status", HttpServletResponse.SC_OK);
        return new ModelAndView("manage", model);
    }

    /**
     * Gets services.
     *
     * @param response the response
     */
    @RequestMapping(value="getServices.html", method={RequestMethod.GET})
    public void getServices(final HttpServletResponse response) {
        ensureDefaultServiceExists();
        final Map<String, Object> model = new HashMap<>();
        final List<RegisteredServiceViewBean> serviceBeans = new ArrayList<>();
        final List<RegisteredService> services = new ArrayList<>(this.servicesManager.getAllServices());
        for (final RegisteredService svc : services) {
            serviceBeans.add(RegisteredServiceViewBean.fromRegisteredService(svc));
        }
        model.put("services", serviceBeans);
        model.put("status", HttpServletResponse.SC_OK);
        JsonViewUtils.render(model, response);
    }

    /**
     * Updates the {@link RegisteredService#getEvaluationOrder()}.
     *
     * @param response the response
     * @param id the service ids, whose order also determines the service evaluation order
     */
    @RequestMapping(value="updateRegisteredServiceEvaluationOrder.html", method={RequestMethod.POST})
    public void updateRegisteredServiceEvaluationOrder(final HttpServletResponse response,
                                                       @RequestParam("id") final long... id) {
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
        final Map<String, Object> model = new HashMap<>();
        model.put("status", HttpServletResponse.SC_OK);
        JsonViewUtils.render(model, response);
    }


}
