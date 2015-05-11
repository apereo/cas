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

import lombok.extern.slf4j.Slf4j;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SimpleFormController to handle adding/editing of RegisteredServices.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@Controller
public final class RegisteredServiceSimpleFormController {

    private static final String COMMAND_NAME = "registeredService";

    /** Instance of ServiceRegistryManager. */
    @NotNull
    private final ServicesManager servicesManager;

    /** Instance of AttributeRegistry. */
    @NotNull
    private final IPersonAttributeDao personAttributeDao;
    
    @NotNull
    @Resource(name="registeredServiceValidator")
    private Validator validator;

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager the services manager
     * @param personAttributeDao the attribute repository
     */
    @Autowired
    public RegisteredServiceSimpleFormController(final ServicesManager servicesManager, 
            final IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
        this.servicesManager = servicesManager;
    }
    
    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager the services manager
     * @param personAttributeDao the person attribute dao
     * @param validator the validator
     */
    RegisteredServiceSimpleFormController(final ServicesManager servicesManager, 
            final IPersonAttributeDao personAttributeDao, final Validator validator) {
        this(servicesManager, personAttributeDao);
        this.validator = validator;
    }
    
    /**
     * Sets the require fields and the disallowed fields from the
     * HttpServletRequest.
     *
     * @param request the request
     * @param binder the binder
     * @throws Exception the exception
     */
    @InitBinder
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) throws Exception {
        binder.setRequiredFields("description", "serviceId",
                "name", "enabled", "ssoEnabled",
                "anonymousAccess", "evaluationOrder");
        binder.setDisallowedFields("id");
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /**
     * Adds the service to the ServiceRegistry via the ServiceRegistryManager.
     *
     * @param service the service
     * @param result the binding result
     * @param model the page model
     * @param request the http request
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute(COMMAND_NAME) final RegisteredService service,
            final BindingResult result, final ModelMap model, final HttpServletRequest request) throws Exception {
        
        updateModelMap(model, request);
        
        this.validator.validate(service, result);
        if (result.hasErrors()) {
            model.addAttribute("validationErrors", result.getAllErrors());
            return render(request, model);
        }
       
        RegisteredService svcToUse = service;
        if (service.getServiceId().startsWith("^") && service instanceof RegisteredServiceImpl) {
            logger.debug("Detected regular expression starting with ^");
            final RegexRegisteredService regexService = new RegexRegisteredService();
            regexService.copyFrom(service);
            svcToUse = regexService;
        } else if (!service.getServiceId().startsWith("^") && service instanceof RegexRegisteredService) {
            logger.debug("Detected ant expression {}", service.getServiceId());
            final RegisteredServiceImpl regexService = new RegisteredServiceImpl();
            regexService.copyFrom(service);
            svcToUse = regexService;
        } 

        this.servicesManager.save(svcToUse);
        logger.info("Saved changes to service {}", svcToUse.getId());

        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
                "manage.html#" + svcToUse.getId(), true));
        modelAndView.addObject("action", "add");
        modelAndView.addObject("id", svcToUse.getId());

        
        
        return modelAndView;
    }

    /**
     * Render the page.
     * The view is first updated by {@link #updateModelMap(ModelMap, HttpServletRequest)}.
     * @param request the request
     * @param model the model
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method=RequestMethod.GET, value= {"add.html", "edit.html"})
    protected ModelAndView render(final HttpServletRequest request, final ModelMap model)
            throws Exception {
        updateModelMap(model, request);
        return new ModelAndView("add");
    }
    
    /**
     * Updates model map. The following objects will be available in the model:
     * 
     * <ul>
     *  <li><code>availableAttributes</code></li>
     *  <li><code>availableUsernameAttributes</code></li>
     *  <li><code>pageTitle</code></li>
     * </ul>
     *
     * @param model the model
     * @param request the request
     */
    private void updateModelMap(final ModelMap model, final HttpServletRequest request) {
        final List<String> possibleAttributeNames = new ArrayList<>();
        possibleAttributeNames.addAll(this.personAttributeDao.getPossibleUserAttributeNames());
        Collections.sort(possibleAttributeNames);
        model.addAttribute("availableAttributes", possibleAttributeNames);

        final List<String> possibleUsernameAttributeNames = new ArrayList<>();
        possibleUsernameAttributeNames.addAll(possibleAttributeNames);
        possibleUsernameAttributeNames.add(0, "");
        model.addAttribute("availableUsernameAttributes", possibleUsernameAttributeNames);
        
        final String path = request.getServletPath().replace("/", "").replace(".html", "").concat("ServiceView");
        model.addAttribute("pageTitle", path);    
    }
    
    /**
     * Determines the registered service to be used. 
     * If no <code>id</code> is specified, a service of type
     * {@link RegisteredServiceImpl} will be created by default.
     *
     * @param id the id
     * @return the service
     */
    @ModelAttribute(COMMAND_NAME)
    public RegisteredService getCommand(@RequestParam(value="id", required=false) final String id) {

        if (!StringUtils.hasText(id)) {
            final RegisteredService service = new RegisteredServiceImpl();
            logger.debug("Created new service of type {}", service.getClass().getName());
            return service;
        }

        final RegisteredService service = this.servicesManager.findServiceBy(Long.parseLong(id));

        if (service != null) {
            logger.debug("Loaded service {}", service.getServiceId());
        } else {
            logger.debug("Invalid service id specified.");
        }

        return service;
    }
}
