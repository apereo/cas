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
import org.springframework.util.Assert;

/**
 * Advice to check if a service is allowed to use the
 * CentralAuthenticationService. This advice's pointcut is the exection of two
 * methods: either *CentralAuthenticationService.grantServiceTicket.* or
 * .*CentralAuthenticationService.validateServiceTicket.*.
 * <p>
 * Behavior is undefined if you apply the advice to another method. Subclasses
 * may override beforeInternal and provide additional processing that may be
 * defined for other methods.
 * </p>
 * <p>
 * This advice will retrieve the Service from the list of parameters and attempt
 * to locate it in the registry by matching Ids. If a match is found, the call
 * to the method is allowed to proceed.
 * </p>
 * <p>
 * This class requires the following properties to be set:
 * </p>
 * <ul>
 * <li>serviceRegistry - the ServiceRegistry that contains the registered
 * services.</li>
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceAllowedMethodBeforeAdvice implements MethodBeforeAdvice,
    InitializingBean {

    /** The registry containing the list of services. */
    private ServiceRegistry serviceRegistry;

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
        final Object target, final RegisteredService service) {
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

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(this.serviceRegistry,
            "serviceRegistry is a required property.");

        afterPropertiesSetInternal();
    }

    public void afterPropertiesSetInternal() throws Exception {
        // designed to be overwritten
    }
}
