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

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean;
import org.jasig.cas.web.view.JsonViewUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handle adding/editing of RegisteredServices.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller
public final class RegisteredServiceSimpleFormController extends AbstractManagementController {

    /**
     * Instance of AttributeRegistry.
     */
    @NotNull
    private final IPersonAttributeDao personAttributeDao;

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager    the services manager
     * @param personAttributeDao the attribute repository
     */
    @Autowired
    public RegisteredServiceSimpleFormController(
        final ReloadableServicesManager servicesManager,
        final IPersonAttributeDao personAttributeDao) {
        super(servicesManager);
        this.personAttributeDao = personAttributeDao;
    }

    /**
     * Adds the service to the Service Registry.
     * @param request the request
     * @param response the response
     * @param result the result
     * @param service the edit bean
     */
    @RequestMapping(method = RequestMethod.POST, value = {"saveService.html"})
    public void saveService(final HttpServletRequest request,
                            final HttpServletResponse response,
                            @RequestBody final RegisteredServiceEditBean.ServiceData service,
                            final BindingResult result) {
        try {

            final RegisteredService svcToUse = service.toRegisteredService(this.personAttributeDao);

            final RegisteredService newSvc = this.servicesManager.save(svcToUse);
            logger.info("Saved changes to service {}", svcToUse.getId());

            final Map<String, Object> model = new HashMap<>();
            model.put("id", newSvc.getId());
            model.put("status", HttpServletResponse.SC_OK);
            JsonViewUtils.render(model, response);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets service by id.
     *
     * @param id       the id
     * @param request  the request
     * @param response the response
     */
    @RequestMapping(method = RequestMethod.GET, value = {"getService.html"})
    public void getServiceById(@RequestParam(value = "id", required = false) final Long id,
                               final HttpServletRequest request, final HttpServletResponse response) {

        try {
            RegisteredServiceEditBean bean = null;
            if (id == -1) {
                bean = new RegisteredServiceEditBean();
                bean.setServiceData(null);
            } else {
                final RegisteredService service = this.servicesManager.findServiceBy(id);

                if (service == null) {
                    logger.warn("Invalid service id specified [{}]. Cannot find service in the registry", id);
                    throw new IllegalArgumentException("Service id cannot be found");
                }
                bean = RegisteredServiceEditBean.fromRegisteredService(service);
            }
            final RegisteredServiceEditBean.FormData formData = bean.getFormData();
            final List<String> possibleAttributeNames = new ArrayList<>(this.personAttributeDao.getPossibleUserAttributeNames());
            Collections.sort(possibleAttributeNames);
            formData.setAvailableAttributes(possibleAttributeNames);

            bean.setStatus(HttpServletResponse.SC_OK);
            JsonViewUtils.render(bean, response);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
