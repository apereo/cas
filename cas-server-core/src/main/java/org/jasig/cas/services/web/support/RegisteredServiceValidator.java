/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web.support;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * RegisteredServiceValidator ensures that a new RegisteredService does not have
 * a conflicting Service Id with another service already in the registry.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class RegisteredServiceValidator implements Validator {

    /** Default length, which matches what is in the view. */
    private static final int DEFAULT_MAX_DESCRIPTION_LENGTH = 300;

    /** ServiceRegistry to look up services. */
    @NotNull
    private ServicesManager servicesManager;

    /** The maximum length of the description we will accept. */
    @Min(0)
    private int maxDescriptionLength = DEFAULT_MAX_DESCRIPTION_LENGTH;

    /**
     * Supports the RegisteredServiceImpl.
     * 
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    public boolean supports(final Class clazz) {
        return RegisteredServiceImpl.class.equals(clazz);
    }

    public void validate(final Object o, final Errors errors) {
        final RegisteredService r = (RegisteredService) o;

        if (r.getServiceId() != null) {
            for (final RegisteredService service : this.servicesManager
                .getAllServices()) {
                if (r.getServiceId().equals(service.getServiceId())
                    && r.getId() != service.getId()) {
                    errors.rejectValue("serviceId",
                        "registeredService.serviceId.exists", null);
                    break;
                }
            }
        }

        if (r.getDescription() != null
            && r.getDescription().length() > this.maxDescriptionLength) {
            errors.rejectValue("description",
                "registeredService.description.length", null);
        }
    }

    public void setServicesManager(final ServicesManager serviceRegistry) {
        this.servicesManager = serviceRegistry;
    }

    public void setMaxDescriptionLength(final int maxLength) {
        this.maxDescriptionLength = maxLength;
    }
}
