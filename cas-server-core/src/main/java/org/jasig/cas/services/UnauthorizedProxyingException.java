/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * Exception thrown when a service attempts to proxy when it is not allowed to.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class UnauthorizedProxyingException extends UnauthorizedServiceException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7307803750894078575L;

    /** The code description. */
    private static final String CODE = "service.not.authorized.proxy";

    public UnauthorizedProxyingException() {
        super(CODE);
    }

    public UnauthorizedProxyingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedProxyingException(String message) {
        super(message);
    }
}
