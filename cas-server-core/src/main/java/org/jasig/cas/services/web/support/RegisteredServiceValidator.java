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


public class RegisteredServiceValidator implements Validator, InitializingBean {
    
    private ServiceRegistry serviceRegistry;

    public boolean supports(final Class clazz) {
        return RegisteredServiceImpl.class.equals(clazz);
    }

    public void validate(final Object o, final Errors errors) {
        final RegisteredService r = (RegisteredService) o;
        
        if (r.getServiceId() != null) {
            for (final RegisteredService service : this.serviceRegistry.getAllServices()) {
                if (r.getServiceId().equals(service.getServiceId())) {
                    errors.rejectValue("serviceId", "registeredService.serviceId.exists", null);
                    break;
                }
            }
        }
        
        
        // TODO Auto-generated method stub

    }
    
    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.serviceRegistry, "serviceRegistry cannot be null.");
    }
}
