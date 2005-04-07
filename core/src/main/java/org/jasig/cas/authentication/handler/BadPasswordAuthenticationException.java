/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * The exception to throw when we know the username is correct but the password
 * is not.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BadPasswordAuthenticationException extends
    BadUsernameOrPasswordAuthenticationException {

    /** Unique ID for serializing. */
    private static final long serialVersionUID = 3977861752513837361L;

    /** The code description of this exception. */
    private static final String CODE = "error.authentication.credentials.bad.usernameorpassword.password";

    public BadPasswordAuthenticationException() {
        super(CODE);
    }

    public BadPasswordAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }

    public BadPasswordAuthenticationException(final String code) {
        super(code);
    }

    public BadPasswordAuthenticationException(final String code,
        final Throwable throwable) {
        super(code, throwable);
    }
}
