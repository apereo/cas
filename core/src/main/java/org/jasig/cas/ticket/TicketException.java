/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.io.Serializable;

/**
 * Generic ticket exception. Top of the TicketException heirarchy.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketException extends Exception {

    /** Serializable Unique ID */
    private static final long serialVersionUID = -6000583436059919480L;

    /** Exception code to denote invalid ticket. */
    public static final TicketExceptionCode INVALID_TICKET = new TicketExceptionCode(
        "INVALID_TICKET");

    /** Excepton code to denote invalid service. */
    public static final TicketExceptionCode INVALID_SERVICE = new TicketExceptionCode(
        "INVALID_SERVICE");

    /** The specific code for this exception instance. */
    private TicketExceptionCode code;

    /** An additional textual description of the code. */
    private String description;

    /**
     * 
     */
    public TicketException() {
        super();
    }

    /**
     * 
     */
    public TicketException(TicketExceptionCode code, final String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @param message
     */
    public TicketException(final String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public TicketException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TicketException(final Throwable cause) {
        super(cause);
    }

    public TicketException(final String message,
        final TicketExceptionCode code, final String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    /**
     * @param message
     * @param cause
     */
    public TicketException(final String message, final Throwable cause,
        final TicketExceptionCode code, final String description) {
        super(message, cause);
        this.code = code;
        this.description = description;

    }

    /**
     * @param cause
     */
    public TicketException(final Throwable cause,
        final TicketExceptionCode code, final String description) {
        super(cause);
        this.code = code;
        this.description = description;

    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return this.code.toString();
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    protected static class TicketExceptionCode implements Serializable {

        /** Serializable Unique ID */
        private static final long serialVersionUID = 3258413923983045424L;

        /** Code of the exception. */
        private String code;

        protected TicketExceptionCode(String code) {
            this.code = code;
        }

        public String toString() {
            return this.code;
        }
    }
}
