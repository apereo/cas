/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation.support;

import org.jasig.cas.ticket.ProxyTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.validation.AbstractTicketValidatorHelper;
import org.jasig.cas.ticket.validation.ValidationRequest;

/**
 * TicketValidatorHelper for <code>ServiceTicket</code> s
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.ServiceTicket
 */
public class ServiceTicketValidatorHelper extends AbstractTicketValidatorHelper {

	/**
	 * 
	 * @see org.jasig.cas.ticket.validation.AbstractTicketValidatorHelper#isValidForRequestInternal(org.jasig.cas.ticket.Ticket, org.jasig.cas.ticket.validation.ValidationRequest)
	 */
    protected boolean isValidForRequestInternal(final Ticket ticket, final ValidationRequest request) {
        final ServiceTicket serviceTicket = (ServiceTicket) ticket;
        if (request.isRenew() && !serviceTicket.isFromNewLogin())
            return false;

        if (serviceTicket.getService().equals(request.getService())) {
        	logger.debug("ServiceTicket [" + ticket.getId() + "] successfully validated.");
        	return true;
        }
        else {
        	logger.debug("ServiceTicket [" + ticket.getId() + "] service of [" + serviceTicket.getService() + "] does not match validation request of [" + request.getService() + "]");
        	return false;
        }
    }

    /**
     * 
     * @see org.jasig.cas.ticket.validation.TicketValidatorHelper#supports(org.jasig.cas.ticket.Ticket)
     */
    public boolean supports(final Ticket ticket) {
        return ticket.getClass().equals(ServiceTicket.class) || ticket.getClass().equals(ProxyTicket.class);
    }
}