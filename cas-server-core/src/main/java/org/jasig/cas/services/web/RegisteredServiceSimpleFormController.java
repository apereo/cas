/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.Attribute;
import org.jasig.cas.services.AttributeRepository;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.web.support.AttributePropertyEditor;
import org.jasig.cas.util.annotation.NotNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * SimpleFormController to handle adding/editing of RegisteredServices.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class RegisteredServiceSimpleFormController extends
    SimpleFormController {

    /** Instance of ServiceRegistryManager */
    @NotNull
    private final ServicesManager servicesManager;

    /** Instance of AttributeRegistry. */
    @NotNull
    private final AttributeRepository attributeRepository;

    public RegisteredServiceSimpleFormController(
        final ServicesManager servicesManager,
        final AttributeRepository attributeRepository) {
        this.servicesManager = servicesManager;
        this.attributeRepository = attributeRepository;
    }

    /**
     * Sets the require fields and the disallowed fields from the
     * HttpServletRequest.
     * 
     * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
     * org.springframework.web.bind.ServletRequestDataBinder)
     */
    protected final void initBinder(final HttpServletRequest request,
        final ServletRequestDataBinder binder) throws Exception {
        binder.setRequiredFields(new String[] {"description", "serviceId",
            "name", "allowedToProxy", "enabled", "ssoEnabled",
            "anonymousAccess"});
        binder.setDisallowedFields(new String[] {"id"});
        binder.registerCustomEditor(Attribute.class, new AttributePropertyEditor(this.attributeRepository));
    }

    /**
     * Adds the service to the ServiceRegistry via the ServiceRegistryManager.
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.Object,
     * org.springframework.validation.BindException)
     */
    protected final ModelAndView onSubmit(final HttpServletRequest request,
        final HttpServletResponse response, final Object command,
        final BindException errors) throws Exception {
        final RegisteredService service = (RegisteredService) command;

        this.servicesManager.save(service);

        final ModelAndView modelAndView = new ModelAndView(new RedirectView(
            "/services/manage.html#" + service.getId(), true));
        modelAndView.addObject("action", "add");
        modelAndView.addObject("id", new Long(service.getId()));

        return modelAndView;
    }

    protected Object formBackingObject(final HttpServletRequest request)
        throws Exception {
        final String id = request.getParameter("id");

        if (!StringUtils.hasText(id)) {
            return new RegisteredServiceImpl();
        }

        final long serviceId = Long.parseLong(id);

        return this.servicesManager
            .findServiceBy(serviceId);
    }

    /**
     * Returns the attributes, page title, and command name.
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
     */
    protected final Map referenceData(final HttpServletRequest request)
        throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        model
            .put("availableAttributes", this.attributeRepository.getAttributes());
        model.put("pageTitle", getFormView());
        model.put("commandName", getCommandName());
        return model;
    }
}
