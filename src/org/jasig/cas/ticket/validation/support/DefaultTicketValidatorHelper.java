/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation.support;

import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.validation.AbstractTicketValidatorHelper;
import org.jasig.cas.ticket.validation.ValidationRequest;

/**
 * TicketValidatorHelper for <code>ProxyGrantingTicket</code> s
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.ProxyGrantingTicket
 */
public class DefaultTicketValidatorHelper extends AbstractTicketValidatorHelper {

	/**
	 * 
	 * @see org.jasig.cas.ticket.validation.AbstractTicketValidatorHelper#isValidForRequestInternal(org.jasig.cas.ticket.Ticket, org.jasig.cas.ticket.validation.ValidationRequest)
	 */
    protected boolean isValidForRequestInternal(final Ticket ticket, final ValidationRequest request) {
        // only uses expiration test from abstract
        return true;
    }

    /**
     * @see org.jasig.cas.ticket.validation.TicketValidatorHelper#supports(org.jasig.cas.domain.Ticket)
     */
    public boolean supports(final Ticket ticket) {
        return ticket.getClass().equals(ProxyGrantingTicket.class);
    }
}