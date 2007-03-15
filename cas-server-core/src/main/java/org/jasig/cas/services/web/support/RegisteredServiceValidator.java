/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web.support;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServiceRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * RegisteredServiceValidator ensures that a new RegisteredService does not have
 * a conflicting Service Id with another service already in the registry.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class RegisteredServiceValidator implements Validator, InitializingBean {

    private static final int DEFAULT_MAX_DESCRIPTION_LENGTH = 300;

    /** ServiceRegistry to look up services. */
    private ServiceRegistry serviceRegistry;

    private int maxDescriptionLength = DEFAULT_MAX_DESCRIPTION_LENGTH;

    public boolean supports(final Class clazz) {
        return RegisteredServiceImpl.class.equals(clazz);
    }

    public void validate(final Object o, final Errors errors) {
        final RegisteredService r = (RegisteredService) o;

        if (r.getServiceId() != null) {
            for (final RegisteredService service : this.serviceRegistry
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

    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setMaxDescriptionLength(final int maxLength) {
        this.maxDescriptionLength = maxLength;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.serviceRegistry, "serviceRegistry cannot be null.");
        Assert.isTrue(this.maxDescriptionLength > 0, "maxDescriptionLength must be greater than 0.");
    }
}
