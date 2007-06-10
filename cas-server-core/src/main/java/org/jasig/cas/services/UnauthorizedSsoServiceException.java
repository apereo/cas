/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * Exception thrown when a service attempts to use SSO when it should not be
 * allowed to.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class UnauthorizedSsoServiceException extends
    UnauthorizedServiceException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8909291297815558561L;

    /** The code description. */
    private static final String CODE = "service.not.authorized.sso";

    public UnauthorizedSsoServiceException() {
        this(CODE);
    }

    public UnauthorizedSsoServiceException(final String message,
        final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedSsoServiceException(final String message) {
        super(message);
    }

}
