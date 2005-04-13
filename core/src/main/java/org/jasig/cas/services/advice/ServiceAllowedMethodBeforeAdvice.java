/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * Advice to check if a service is allowed to use CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceAllowedMethodBeforeAdvice implements MethodBeforeAdvice,
    InitializingBean {

    /** The registry containing the list of services. */
    private ServiceRegistry serviceRegistry;

    /**
     * @see org.springframework.aop.MethodBeforeAdvice#before(java.lang.reflect.Method,
     * java.lang.Object[], java.lang.Object)
     */
    public final void before(final Method method, final Object[] args,
        final Object target) throws UnauthorizedServiceException {
        final Service service = (Service) args[1];
        final RegisteredService authenticatedService = this.serviceRegistry
            .getService(service.getId());

        if (authenticatedService == null) {
            throw new UnauthorizedServiceException("Service: ["
                + service.getId() + "] not found in registry.");
        }

        beforeInternal(method, args, target, authenticatedService);
    }

    protected void beforeInternal(final Method method, final Object[] args,
        final Object target, final RegisteredService service)
        throws UnauthorizedServiceException {
        // this will be overwritten by extending classes
    }

    /**
     * @return Returns the serviceRegistry.
     */
    public final ServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }

    /**
     * @param serviceRegistry The serviceRegistry to set.
     */
    public final void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public final void afterPropertiesSet() throws Exception {
        if (this.serviceRegistry == null) {
            throw new IllegalStateException(
                "ServiceRegistry cannot be null on "
                    + this.getClass().getName());
        }

        afterPropertiesSetInternal();
    }

    public void afterPropertiesSetInternal() throws Exception {
        // designed to be overwritten
    }
}
