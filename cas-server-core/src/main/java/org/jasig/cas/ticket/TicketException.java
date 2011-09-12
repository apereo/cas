/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

/**
 * Generic ticket exception. Top of the TicketException heirarchy.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class TicketException extends Exception {

    /** Serializable Unique ID. */
    private static final long serialVersionUID = -6000583436059919480L;

    /** The code description of the TicketException. */
    private String code;

    /**
     * Constructs a new TicketException with the code identifying the exception
     * type.
     * 
     * @param code the code to describe what type of exception this is.
     */
    public TicketException(final String code) {
        this.code = code;
    }

    /**
     * Constructs a new TicketException with the code identifying the exception
     * and the original Throwable.
     * 
     * @param code the code to describe what type of exception this is.
     * @param throwable the original exception we are chaining.
     */
    public TicketException(final String code, final Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    /**
     * @return Returns the code. If there is a chained exception it returns the
     * toString-ed version of the chained exception rather than the code.
     */
    public final String getCode() {
        return (this.getCause() != null) ? this.getCause().toString()
            : this.code;
    }
}
