/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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

    /** Serializable Unique ID */
    private static final long serialVersionUID = -6000583436059919480L;

    /** The code description of the TicketException. */
    private String code;

    public TicketException(final String code) {
        this.code = code;
    }
    
    public TicketException(final String code, final Throwable throwable) {
        super(throwable);
        this.code = code;
    }
    
     /**
     * @return Returns the code.
     */
    public final String getCode() {
        return (this.getCause() != null) ? this.getCause().toString() : this.code;
    }
}
