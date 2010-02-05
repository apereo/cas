/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import org.inspektr.common.spi.PrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.AopUtils;

import javax.validation.constraints.NotNull;

/**
 * PrincipalResolver that can retrieve the username from either the Ticket or from the Credentials.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class TicketOrCredentialPrincipalResolver implements PrincipalResolver {
    
    @NotNull
    protected TicketRegistry ticketRegistry;
    
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public String resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return resolveFromInternal(AopUtils.unWrapJoinPoint(joinPoint));
    }

    public String resolveFrom(final JoinPoint joinPoint, Exception retval) {
        return resolveFromInternal(AopUtils.unWrapJoinPoint(joinPoint));
    }

    public String resolve() {
        return UNKNOWN_USER;
    }
    
    protected String resolveFromInternal(final JoinPoint joinPoint) {
        String resource = "";
        final Object arg1 = joinPoint.getArgs()[0];
        if (arg1 instanceof Credentials) {
            resource = arg1.toString();
        } else if (arg1 instanceof String) {
            final Ticket ticket = this.ticketRegistry.getTicket((String) arg1);
            if (ticket instanceof ServiceTicket) {
                final ServiceTicket serviceTicket = (ServiceTicket) ticket;
                resource = serviceTicket.getGrantingTicket().getAuthentication().getPrincipal().getId(); 
            } else if (ticket instanceof TicketGrantingTicket) {
                final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
                resource = tgt.getAuthentication().getPrincipal().getId();
            }
        }
        return resource;
    }
}
