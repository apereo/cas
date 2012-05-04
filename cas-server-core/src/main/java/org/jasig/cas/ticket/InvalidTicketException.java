/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

/**
 * TicketException to alert that a Ticket was not found or that it is expired.
 * 
 * @author Scott Battaglia
 * @version $Revison$ $Date$
 * @since 3.0
 */
public class InvalidTicketException extends TicketException {

    /** The Unique Serializable ID. */
    private static final long serialVersionUID = 3256723974594508849L;

    /** The code description. */
    private static final String CODE = "INVALID_TICKET";

    /**
     * Constructs a InvalidTicketException with the default exception code.
     */
    public InvalidTicketException() {
        super(CODE);
    }

    /**
     * Constructs a InvalidTicketException with the default exception code and
     * the original exception that was thrown.
     * 
     * @param throwable the chained exception
     */
    public InvalidTicketException(final Throwable throwable) {
        super(CODE, throwable);
    }
}
