/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * Generic exception when we can't determine the authenticity of the
 * credentials.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BadCredentialsAuthenticationException extends
    AuthenticationException {

    /** UID for serializable objects. */
    private static final long serialVersionUID = 3256719585087797044L;

    /** String code to map to error message. */
    private static final String CODE = "error.authentication.credentials.bad";

    public BadCredentialsAuthenticationException() {
        super(CODE);
    }

    public BadCredentialsAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }

    public BadCredentialsAuthenticationException(final String code) {
        super(code);
    }

    public BadCredentialsAuthenticationException(final String code,
        final Throwable throwable) {
        super(code, throwable);
    }
}
