/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * @author Scott Battaglia
 * @version $Id: UnauthorizedServiceException.java,v 1.1 2005/03/05 00:38:52
 * sbattaglia Exp $
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
