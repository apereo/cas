/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.Ticket;

/**
 * Default implementation of {@link TicketValidator}
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class TicketValidatorImpl implements TicketValidator {

    protected final Log log = LogFactory.getLog(getClass());

    private List ticketValidatorHelpers;

    /**
     * @see org.jasig.cas.ticket.validation.TicketValidator#validate(org.jasig.cas.ticket.Ticket, org.jasig.cas.ticket.validation.ValidationRequest)
     */
    public boolean validate(final Ticket ticket, final ValidationRequest request) {
        log.debug("Attempting to find validator for ticket of type [" + ticket.getClass().getName() + "]");
        for (Iterator iter = this.ticketValidatorHelpers.iterator(); iter.hasNext();) {
            final TicketValidatorHelper helper = (TicketValidatorHelper)iter.next();

            if (helper.supports(ticket)) {
                log.debug("Found validator of type [" + helper.getClass().getName() + "] for [" + ticket.getClass().getName() + "]");
                return (helper.validateForRequest(ticket, request));
            }
        }

        log.error("No validator registered for [" + ticket.getClass().getName() + "].  Assuming configuration error so rejecting ticket.");
        return false;
    }

    /**
     * @param ticketValidatorHelpers The ticketValidatorHelpers to set.
     */
    public void setTicketValidatorHelpers(final List ticketValidatorHelpers) {
        this.ticketValidatorHelpers = ticketValidatorHelpers;
    }
}