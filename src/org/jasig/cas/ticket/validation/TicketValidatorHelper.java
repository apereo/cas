/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation;

import org.jasig.cas.ticket.Ticket;

/**
 * Helper to validate different types of <code>Ticket</code>s.
 * <p>
 * Note that this is a SPI interface used internally by <code>TicketValidator</code>
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.Ticket
 * @see org.jasig.cas.ticket.validation.ValidationRequest
 */
public interface TicketValidatorHelper {

    /**
     * Validate a given Ticket for a given ValidationRequest
     * 
     * @param ticket to validate
     * @param validationRequest a spec for a Ticket validation
     * @return whether a given ticket is valid for a given validation request
     */
    boolean validateForRequest(Ticket ticket, ValidationRequest validationRequest);

    /**
     * Does this helper support a particular Ticket type
     * 
     * @param ticket in question
     * @return true or false
     */
    boolean supports(Ticket ticket);
}