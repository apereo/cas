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
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.factory.TicketCreator;


/**
 * TicketCreator for TicketGrantingTicket
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.TicketGrantingTicket
 */
public class TicketGrantingTicketCreator implements TicketCreator {
	protected final Log logger = LogFactory.getLog(getClass());
    private static final String PREFIX = "TGT";
    private ExpirationPolicy policy;

    /**
     * @param policy The policy to set.
     */
    public void setPolicy(ExpirationPolicy policy) {
        this.policy = policy;
    }

	/**
	 * 
	 * @see org.jasig.cas.ticket.factory.TicketCreator#createTicket(org.jasig.cas.authentication.principal.Principal, org.jasig.cas.ticket.CasAttributes, java.lang.String, org.jasig.cas.ticket.Ticket)
	 */
    public Ticket createTicket(final Principal principal, final CasAttributes casAttributes, final String ticketId, final Ticket grantingTicket) {

        logger.debug("Creating TicketGrantingTicket for ID [" + ticketId + "]");
        return new TicketGrantingTicketImpl(ticketId, principal, policy);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.factory.TicketCreator#supports(java.lang.Class)
     */
    public boolean supports(final Class clazz) {
        return TicketGrantingTicket.class.equals(clazz);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.factory.TicketCreator#getPrefix()
     */
    public String getPrefix() {
        return PREFIX;
    }

}