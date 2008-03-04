/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Abstract Implementation that handles some of the commonalities between
 * distributed ticket registries.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractDistributedTicketRegistry extends
    AbstractTicketRegistry {
    
    protected static final Method[] SERVICE_TICKET_METHODS = new Method[2];
    
    protected static final Method[] TICKET_GRANTING_TICKET_METHODS = new Method[2];
    
    protected final Log log = LogFactory.getLog(this.getClass());
    
    static {
        try {
            SERVICE_TICKET_METHODS[0] = ServiceTicket.class.getMethod("isValidFor",
                new Class<?>[] {Service.class});
            SERVICE_TICKET_METHODS[1] = ServiceTicket.class.getMethod(
                "grantTicketGrantingTicket", new Class<?>[] {String.class,
                    Authentication.class, ExpirationPolicy.class});
            
            TICKET_GRANTING_TICKET_METHODS[0] = TicketGrantingTicket.class.getMethod("expire", (Class<?>[]) null);
            TICKET_GRANTING_TICKET_METHODS[1] = TicketGrantingTicket.class.getMethod("grantServiceTicket", new Class<?>[] {String.class, Service.class,ExpirationPolicy.class, boolean.class});

        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void updateTicket(final Ticket ticket);

    protected Ticket getProxiedTicketInstance(final Ticket ticket) {
        return (Ticket) ProxiedTicket.newInstance(ticket,
            this);
    }

    private static final class ProxiedTicket implements
        InvocationHandler {

        private final Object ticket;

        private final AbstractDistributedTicketRegistry ticketRegistry;
        
        private final Method[] methods;

        public static Object newInstance(final Object obj,
            final AbstractDistributedTicketRegistry ticketRegistry) {
            if (obj == null) {
                return null;
            }
            
            return java.lang.reflect.Proxy.newProxyInstance(obj.getClass()
                .getClassLoader(), obj.getClass().getInterfaces(),
                new ProxiedTicket(obj, ticketRegistry, obj instanceof ServiceTicket ? SERVICE_TICKET_METHODS : TICKET_GRANTING_TICKET_METHODS));
        }

        private ProxiedTicket(final Object serviceTicket,
            final AbstractDistributedTicketRegistry ticketRegistry, final Method[] methods) {
            this.ticket = serviceTicket;
            this.ticketRegistry = ticketRegistry;
            this.methods = methods;
        }

        public Object invoke(final Object proxy, final Method m,
            final Object[] args) throws Throwable {
            final Object result = m.invoke(this.ticket, args);
            
            for (final Method method : this.methods) {
                if (method.equals(m)) {
                    this.ticketRegistry.updateTicket((Ticket) this.ticket);    
                }
            }

            return result;
        }
    }
}
