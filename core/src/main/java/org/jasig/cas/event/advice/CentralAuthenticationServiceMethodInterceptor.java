/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jasig.cas.event.TicketEvent;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class CentralAuthenticationServiceMethodInterceptor implements MethodInterceptor, ApplicationEventPublisherAware, InitializingBean {

    /** The TicketRegistry which holds ticket information. */
    private TicketRegistry ticketRegistry;

    /** The publisher to publish events. */
    private ApplicationEventPublisher applicationEventPublisher;
    
    public Object invoke(MethodInvocation method) throws Throwable {
        Ticket ticket = null;
        TicketEvent ticketEvent = null;
        
        if (method.getMethod().getName().equals("validateServiceTicket")) {
            ticket = this.ticketRegistry.getTicket((String) method.getArguments()[0]);
        }
        
        final Object returnValue = method.proceed();
        
        if (!method.getMethod().getName().equals("validateServiceTicket") && !method.getMethod().getName().equals("destroyTicketGrantingTicket")) {
            ticket = this.ticketRegistry.getTicket((String) returnValue);
        }
        
        final String methodName = method.getMethod().getName();
        
        if (methodName.equals("createTicketGrantingTicket")) {
            ticketEvent = new TicketEvent(ticket,
                TicketEvent.CREATE_TICKET_GRANTING_TICKET);
        } else if (methodName.equals("delegateTicketGrantingTicket")) {
            ticketEvent = new TicketEvent(ticket,
                TicketEvent.CREATE_TICKET_GRANTING_TICKET);
        } else if (methodName.equals("grantServiceTicket")) {
            ticketEvent = new TicketEvent(ticket,
                TicketEvent.CREATE_SERVICE_TICKET);
        } else if (methodName.equals("destroyTicketGrantingTicket")) {
            ticketEvent = new TicketEvent(
                TicketEvent.DESTROY_TICKET_GRANTING_TICKET, (String) method.getArguments()[0]);
        } else if (methodName.equals("validateServiceTicket")) {
            ticketEvent = new TicketEvent(ticket,
                TicketEvent.VALIDATE_SERVICE_TICKET);
        }
        
        if (ticketEvent != null) {
            this.applicationEventPublisher.publishEvent(ticketEvent);
        }

        return returnValue;
    }
    
    public void setApplicationEventPublisher(
        final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ticketRegistry,
            "ticketRegistry is a required property.");
    }

    /**
     * The TicketRegistry to use to look up tickets.
     * 
     * @param ticketRegistry the TicketRegistry
     */
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

}
