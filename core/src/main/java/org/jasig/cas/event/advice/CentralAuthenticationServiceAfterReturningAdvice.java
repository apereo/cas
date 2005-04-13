/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import java.lang.reflect.Method;

import org.jasig.cas.event.TicketEvent;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * AfterReturningAdvice to advice the creation, validation, etc. of tickets.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class CentralAuthenticationServiceAfterReturningAdvice implements
    AfterReturningAdvice, ApplicationEventPublisherAware, InitializingBean {
    
    /** The TicketRegistry which holds ticket information. */
    private TicketRegistry ticketRegistry;
    
    /** The publisher to publish events. */
    private ApplicationEventPublisher applicationEventPublisher;

    public void afterReturning(Object returnValue, Method method, Object[] args,
        Object target) throws Throwable {
        
        if (method.getName().equals("createTicketGrantingTicket")) {
            Ticket ticket = this.ticketRegistry.getTicket((String) returnValue);
            TicketEvent ticketEvent = new TicketEvent(ticket, TicketEvent.CREATE_TICKET_GRANTING_TICKET);
            this.applicationEventPublisher.publishEvent(ticketEvent);
        } else if (method.getName().equals("delegateTicketGrantingTicket")) {
            Ticket ticket = this.ticketRegistry.getTicket((String) returnValue);
            TicketEvent ticketEvent = new TicketEvent(ticket, TicketEvent.CREATE_TICKET_GRANTING_TICKET);
            this.applicationEventPublisher.publishEvent(ticketEvent);
        } else if (method.getName().equals("grantServiceTicket")) {
            Ticket ticket = this.ticketRegistry.getTicket((String) returnValue);
            TicketEvent ticketEvent = new TicketEvent(ticket, TicketEvent.CREATE_SERVCE_TICKET);
            this.applicationEventPublisher.publishEvent(ticketEvent);
        } else if (method.getName().equals("destroyTicketGrantingTicket")) {
            TicketEvent ticketEvent = new TicketEvent(TicketEvent.DESTROY_TICKET_GRANTING_TICKET, (String) args[0]);
            this.applicationEventPublisher.publishEvent(ticketEvent);
        } else if (method.getName().equals("validateServiceTicket")) {
            Ticket ticket = this.ticketRegistry.getTicket((String) args[0]);
            TicketEvent ticketEvent = new TicketEvent(ticket, TicketEvent.VALIDATE_SERVICE_TICKET);
            this.applicationEventPublisher.publishEvent(ticketEvent);
        }
    }

    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void afterPropertiesSet() throws Exception {
       if (this.ticketRegistry == null) {
           throw new IllegalStateException("ticketRegistry cannot be null on " + this.getClass().getName());
       }
    }

    /**
     * The TicketRegistry to use to look up tickets.
     * @param ticketRegistry the TicketRegistry
     */
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
