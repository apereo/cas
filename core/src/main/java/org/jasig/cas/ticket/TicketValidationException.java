/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Exception to alert that there was an error validating the ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketValidationException extends TicketException {

    /** Unique Serial ID. */
    private static final long serialVersionUID = 3257004341537093175L;

    /** The code description. */
    private static final String CODE = "VALIDATION_ERROR";

    public TicketValidationException() {
        super(CODE);
    }

    public TicketValidationException(final Throwable throwable) {
        super(CODE, throwable);
    }

}
