/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UnauthorizedServiceException extends Exception {

    private static final long serialVersionUID = 3905807495715960369L;

    public UnauthorizedServiceException() {
        super();
    }

    public UnauthorizedServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedServiceException(String message) {
        super(message);
    }

    public UnauthorizedServiceException(Throwable cause) {
        super(cause);
    }
}
