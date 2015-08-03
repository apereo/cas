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
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean;
import org.jasig.cas.web.view.JsonViewUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @NotNull
    @Resource(name = "registeredServiceValidator")
    private Validator validator;

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager    the services manager
     * @param personAttributeDao the attribute repository
     */
    @Autowired
    public RegisteredServiceSimpleFormController(final ServicesManager servicesManager,
                                                 final IPersonAttributeDao personAttributeDao) {
        super(servicesManager);
        this.personAttributeDao = personAttributeDao;
    }

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager    the services manager
     * @param personAttributeDao the person attribute dao
     * @param validator          the validator
     */
    RegisteredServiceSimpleFormController(final ServicesManager servicesManager,
                                          final IPersonAttributeDao personAttributeDao, final Validator validator) {
        this(servicesManager, personAttributeDao);
        this.validator = validator;
    }

    /**
     * Adds the service to the Service Registry.
     */
    @RequestMapping(method = RequestMethod.POST)
    protected void onSubmit() {
        
    /*
        this.validator.validate(service, result);
        if (result.hasErrors()) {
            model.addAttribute("validationErrors", result.getAllErrors());
            return render(request, model);
        }
       
        RegisteredService svcToUse = service;
        if (service.getServiceId().startsWith("^") && service instanceof RegisteredServiceImpl) {
            LOGGER.debug("Detected regular expression starting with ^");
            final RegexRegisteredService regexService = new RegexRegisteredService();
            regexService.copyFrom(service);
            svcToUse = regexService;
        } else if (!service.getServiceId().startsWith("^") && service instanceof RegexRegisteredService) {
            LOGGER.debug("Detected ant expression {}", service.getServiceId());
            final RegisteredServiceImpl regexService = new RegisteredServiceImpl();
            regexService.copyFrom(service);
            svcToUse = regexService;
        } 

        this.servicesManager.save(svcToUse);
        LOGGER.info("Saved changes to service {}", svcToUse.getId());

        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
                "manage.html#" + svcToUse.getId(), true));
        modelAndView.addObject("action", "add");
        modelAndView.addObject("id", svcToUse.getId());

        
        
        return modelAndView;
        */
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
            final RegisteredService service = this.servicesManager.findServiceBy(id);

            if (service == null) {
                logger.warn("Invalid service id specified [{}]. Cannot find service in the registry", id);
                throw new IllegalArgumentException("Service id cannot be found");
            }

            final RegisteredServiceEditBean bean = RegisteredServiceEditBean.fromRegisteredService(service);
            final List<String> possibleAttributeNames = new ArrayList<>();
            possibleAttributeNames.addAll(this.personAttributeDao.getPossibleUserAttributeNames());
            Collections.sort(possibleAttributeNames);
            bean.setAvailableAttributes(possibleAttributeNames);

            final List<String> possibleUsernameAttributeNames = new ArrayList<>();
            possibleUsernameAttributeNames.addAll(possibleAttributeNames);
            bean.setAvailableUsernameAttributes(possibleUsernameAttributeNames);

            JsonViewUtils.render(bean, response);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
