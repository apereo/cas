/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.Ticket;


/**
 * Skeletal implementation of <code>TicketValidatorHelper</code>.
 * <p>
 * Captures common validation behavior such as determining if a ticket is expired, deferring the more concrete
 * validation for different types of tickets to subclasses.
 * <p>
 * Uses Template Method design pattern.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class AbstractTicketValidatorHelper implements TicketValidatorHelper {
	protected final Log logger = LogFactory.getLog(getClass());
    /**
     * A Template method
     * 
     * @see org.jasig.cas.ticket.validation.TicketValidatorHelper#validateForRequest(org.jasig.cas.domain.Ticket,
     * org.jasig.cas.domain.ValidationRequest)
     */
    public final boolean validateForRequest(final Ticket ticket, final ValidationRequest request) {
        if (ticket.isExpired())
            return false;

        return isValidForRequestInternal(ticket, request);
    }

    /**
     * A hook for subclasses to provide the concrete validation behavior for different types of Tickets.
     */
    protected abstract boolean isValidForRequestInternal(final Ticket ticket, final ValidationRequest request);
}