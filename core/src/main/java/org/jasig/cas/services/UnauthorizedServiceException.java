/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * Exception that is thrown when an Unauthorized Service attempts to use CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UnauthorizedServiceException extends Exception {

    /** The Unique ID for serialization. */
    private static final long serialVersionUID = 3905807495715960369L;

    public UnauthorizedServiceException() {
        super();
    }

    public UnauthorizedServiceException(final String message,
        final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedServiceException(final String message) {
        super(message);
    }

    public UnauthorizedServiceException(final Throwable cause) {
        super(cause);
    }
}
