/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import org.inspektr.audit.spi.AuditablePrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;

import javax.validation.constraints.NotNull;

/**
 * AuditablePrincipalResolver that can retrieve the username from either the Ticket or from the Credentials.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class TicketOrCredentialBasedAuditablePrincipalResolver implements AuditablePrincipalResolver {
    
    @NotNull
    private final TicketRegistry ticketRegistry;
    
    public TicketOrCredentialBasedAuditablePrincipalResolver(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public String resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return resolveFromInternal(joinPoint);
    }

    public String resolveFrom(final JoinPoint joinPoint, Exception retval) {
        return resolveFromInternal(joinPoint);
    }
    
    private String resolveFromInternal(final JoinPoint joinPoint) {
        final Object arg1 = joinPoint.getArgs()[0];
        
        if (arg1 instanceof Credentials) {
            return arg1.toString();
        }
        
        if (arg1 instanceof String) {
            final Ticket ticket = this.ticketRegistry.getTicket((String) arg1);
            
            if (ticket == null) {
                return "";
            }
            
            if (ticket instanceof ServiceTicket) {
                final ServiceTicket serviceTicket = (ServiceTicket) ticket;
                return serviceTicket.getGrantingTicket().getAuthentication().getPrincipal().getId(); 
                
            }
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            return tgt.getAuthentication().getPrincipal().getId();
        }
        return ""; 
    }
}
