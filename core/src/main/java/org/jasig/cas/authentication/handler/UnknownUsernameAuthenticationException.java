/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * The exception to know when we explicitly don't know anything
 * about the username.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UnknownUsernameAuthenticationException extends
    BadUsernameOrPasswordAuthenticationException {

    /** Unique ID for serializing. */
    private static final long serialVersionUID = 3977861752513837361L;

    /** The code description of this exception. */
    private static final String CODE = "error.authentication.credentials.bad.usernameorpassword.password";

    public UnknownUsernameAuthenticationException() {
        super(CODE);
    }
    
    public UnknownUsernameAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }
    
    public UnknownUsernameAuthenticationException(final String code) {
        super(code);
    }
    
    public UnknownUsernameAuthenticationException(final String code, final Throwable throwable) {
        super(code, throwable);
    }
}
