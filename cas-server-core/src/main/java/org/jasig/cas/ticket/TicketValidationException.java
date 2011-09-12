/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;

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
    private static final String CODE = "INVALID_SERVICE";
    
    private final Service service;

    /**
     * Constructs a TicketValidationException with the default exception code
     * and the original exception that was thrown.
     * 
     * @param throwable the chained exception
     */
    public TicketValidationException(final Service service) {
        super(CODE);
        this.service = service;
    }
    
    public Service getOriginalService() {
        return this.service;
    }

}
