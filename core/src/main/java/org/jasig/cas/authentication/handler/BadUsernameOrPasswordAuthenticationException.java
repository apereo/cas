/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * Exception to throw when we know the credentials provided were
 * username/password and the combination is wrong.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BadUsernameOrPasswordAuthenticationException extends
    BadCredentialsAuthenticationException {

    /** Unique ID for serializing. */
    private static final long serialVersionUID = 3977861752513837361L;

    /** The code description of this exception. */
    private static final String CODE = "error.authentication.credentials.bad.usernameorpassword";

    /**
     * 
     */
    public BadUsernameOrPasswordAuthenticationException() {
        super(CODE);
    }
    
    public BadUsernameOrPasswordAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }
    
    public BadUsernameOrPasswordAuthenticationException(final String code) {
        super(code);
    }
    
    public BadUsernameOrPasswordAuthenticationException(final String code, final Throwable throwable) {
        super(code, throwable);
    }
}
