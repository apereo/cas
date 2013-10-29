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
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * SimpleFormController to handle adding/editing of RegisteredServices.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller
public final class RegisteredServiceSimpleFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceSimpleFormController.class);
    
    private static final String COMMAND_NAME = "registeredService";
    private static final String VIEW_NAME = "editServiceView";
    
    /** Instance of ServiceRegistryManager. */
    @NotNull
    @Resource(name="servicesManager")
    private ServicesManager servicesManager;

    /** Instance of AttributeRegistry. */
    @NotNull
    @Resource(name="attributeRepository")
    private IPersonAttributeDao personAttributeDao;
    
    @NotNull
    @Resource(name="registeredServiceValidator")
    private Validator validator;
    
    public RegisteredServiceSimpleFormController() {}
    
    public void setValidator(final Validator v) {
        this.validator = v;
    }
    /**
     * Sets the require fields and the disallowed fields from the
     * HttpServletRequest.
     */
    @InitBinder
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) throws Exception {
        binder.setRequiredFields(new String[] {"description", "serviceId",
                "name", "allowedToProxy", "enabled", "ssoEnabled",
                "anonymousAccess", "evaluationOrder"});
        binder.setDisallowedFields(new String[] {"id"});
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /**
     * Adds the service to the ServiceRegistry via the ServiceRegistryManager.
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute(COMMAND_NAME) final RegisteredService service,
            final BindingResult result) throws Exception {
        
        this.validator.validate(service, result);
        if (result.hasErrors()) {
            return new ModelAndView(VIEW_NAME);
        }
        
        RegisteredService svcToUse = service;
        if (service.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE
                && service.getServiceId().startsWith("^")) {
            LOGGER.debug("Detected regular expression starting with ^");
            final RegexRegisteredService regexService = new RegexRegisteredService();
            regexService.copyFrom(service);
            svcToUse = regexService;
        } 

        this.servicesManager.save(svcToUse);
        LOGGER.info("Saved changes to service " + svcToUse.getId());

        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
                "manage.html#" + svcToUse.getId(), true));
        modelAndView.addObject("action", "add");
        modelAndView.addObject("id", svcToUse.getId());

        return modelAndView;
    }

    @RequestMapping(method=RequestMethod.GET, value= {"add.html", "edit.html"})
    protected Object render(final HttpServletRequest request,
            @RequestParam(value="id", required=false) final String id,
            final ModelMap model)
            throws Exception {
        
        final List<String> possibleAttributeNames = new ArrayList<String>();
        possibleAttributeNames.addAll(this.personAttributeDao.getPossibleUserAttributeNames());
        Collections.sort(possibleAttributeNames);
        model.addAttribute("availableAttributes", possibleAttributeNames);

        final List<String> possibleUsernameAttributeNames = new ArrayList<String>();
        possibleUsernameAttributeNames.addAll(possibleAttributeNames);
        possibleUsernameAttributeNames.add(0, "");
        model.addAttribute("availableUsernameAttributes", possibleUsernameAttributeNames);
        
        final String path = request.getServletPath().replace("/", "").replace(".html", "").concat("ServiceView");
        model.addAttribute("pageTitle", path);

        return new ModelAndView(VIEW_NAME);
    }
    
    @ModelAttribute(COMMAND_NAME)
    public RegisteredService getCommand(@RequestParam(value="id", required=false) final String id) {

        if (!StringUtils.hasText(id)) {
            final RegisteredService service = new RegisteredServiceImpl();
            LOGGER.debug("Created new service of type " + service.getClass().getName());
            return service;
        }

        final RegisteredService service = this.servicesManager.findServiceBy(Long.parseLong(id));

        if (service != null) {
            LOGGER.debug("Loaded service " + service.getServiceId());
        } else {
            LOGGER.debug("Invalid service id specified.");
        }

        return service;
    }
}
