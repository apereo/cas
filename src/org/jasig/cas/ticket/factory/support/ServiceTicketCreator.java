/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.factory.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceRegistry;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.factory.TicketCreator;


/**
 * TicketCreator for ServiceTicket
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.ServiceTicket
 */
public class ServiceTicketCreator implements TicketCreator {
	protected final Log logger = LogFactory.getLog(getClass());
    private static final String PREFIX = "ST";
    private ExpirationPolicy policy;
    private ServiceRegistry serviceRegistry;

    /**
     * @see org.jasig.cas.ticket.factory.TicketCreator#createTicket(org.jasig.cas.domain.TicketCreationAttributes,
     * java.lang.String, org.jasig.cas.domain.Ticket)
     */
    public Ticket createTicket(final Principal principal, final CasAttributes casAttributes, final String ticketId, final Ticket grantingTicket) {
    	final String service = casAttributes.getService();
        logger.debug("Attempting to resolve service id via Service Registry for Service [" + service + "]");
        
        if (!serviceRegistry.serviceExists(service))
            throw new TicketCreationException("A valid service is required to create a service ticket.");

        logger.debug("Creating ServiceTicket for ID [" + ticketId + "]");
        return new ServiceTicketImpl(ticketId, (TicketGrantingTicket) grantingTicket, service, casAttributes.isFirst(), policy);
    }

    /**
     * @see org.jasig.cas.ticket.factory.TicketCreator#supports(java.lang.Class)
     */
    public boolean supports(final Class clazz) {
        return ServiceTicket.class.equals(clazz);
    }

    /**
     * @see org.jasig.cas.ticket.factory.TicketCreator#getPrefix()
     */
    public String getPrefix() {
        return PREFIX;
    }

    /**
     * @param policy The policy to set.
     */
    public void setPolicy(final ExpirationPolicy policy) {
        this.policy = policy;
    }

    /**
     * @param serviceRegistry The serviceRegistry to set.
     */
    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}