/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.jasig.cas.services.AuthenticatedService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public final class ServiceAllowedToProxyMethodBeforeAdvice implements
    MethodBeforeAdvice, InitializingBean {

    private TicketRegistry ticketRegistry;

    private ServiceRegistry serviceRegistry;

    public void afterPropertiesSet() throws Exception {
        if (this.ticketRegistry == null) {
            throw new IllegalStateException("ticketRegistry cannot be null on "
                + this.getClass().getName());
        }

        if (this.serviceRegistry == null) {
            throw new IllegalStateException(
                "serviceRegistry cannot be null on "
                    + this.getClass().getName());
        }
    }

    /**
     * @see org.jasig.cas.services.advice.ServiceAllowedMethodBeforeAdvice#beforeInternal(java.lang.reflect.Method,
     * java.lang.Object[], java.lang.Object,
     * org.jasig.cas.services.AuthenticatedService)
     */
    public void before(Method method, Object[] args, Object target)
        throws Exception {
        String serviceTicketId = (String)args[0];
        ServiceTicket serviceTicket = (ServiceTicket)this.ticketRegistry
            .getTicket(serviceTicketId);
        AuthenticatedService authenticatedService = this.serviceRegistry
            .getService(serviceTicket.getService().getId());

        if ((authenticatedService == null)
            || (!authenticatedService.isAllowedToProxy())) {
            throw new UnauthorizedServiceException(
                "Service is not allowed to proxy.");
        }
    }

    /**
     * @param serviceRegistry The serviceRegistry to set.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param ticketRegistry The ticketRegistry to set.
     */
    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

}
