/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation;

import org.jasig.cas.ticket.Ticket;

/**
 * Helper that validates a <code>Ticket</code> instance for a specific request.
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.Ticket
 * @see org.jasig.cas.ticket.validation.ValidationRequest
 */
public interface TicketValidator {

    /**
     * Determine whether a given Ticket is valid for a given ValidationRequest
     * 
     * @param ticket to validate
     * @param validationRequest encapsulating the rules for validating a given Ticket
     * @return true is Ticket is valid, false otherwise
     */
    public boolean validate(Ticket ticket, ValidationRequest validationRequest);
}