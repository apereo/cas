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
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyTicket;
import org.jasig.cas.ticket.ProxyTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.factory.TicketCreator;


/**
 * TicketCreator for ProxyTicket
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.ProxyTicket
 */
public class ProxyTicketCreator implements TicketCreator {
	protected final Log log = LogFactory.getLog(getClass());
    private static final String PREFIX = "PT";

    private ExpirationPolicy policy;

    /**
     * 
     * @see org.jasig.cas.ticket.factory.TicketCreator#createTicket(org.jasig.cas.authentication.principal.Principal, org.jasig.cas.ticket.CasAttributes, java.lang.String, org.jasig.cas.ticket.Ticket)
     */
    public Ticket createTicket(final Principal principal, CasAttributes casAttributes, final String ticketId, final Ticket grantingTicket) {
    	log.debug("Creating ProxyGrantingTicket with ID [" + ticketId + "]");
        return new ProxyTicketImpl(ticketId, (ProxyGrantingTicket)grantingTicket, casAttributes.getTargetService(), this.policy);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.factory.TicketCreator#supports(java.lang.Class)
     */
    public boolean supports(final Class clazz) {
        return ProxyTicket.class.equals(clazz);
    }

    /**
     * 
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
}