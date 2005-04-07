/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * Exception to represent credentials that have been blocked for a reason such
 * as Locked account.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BlockedCredentialsAuthenticationException extends
    AuthenticationException {

    /** Unique ID for serialization. */
    private static final long serialVersionUID = 3544669598642420017L;

    /** The code description of this exception. */
    private static final String CODE = "error.authentication.credentials.blocked";

    public BlockedCredentialsAuthenticationException() {
        super(CODE);
    }

    public BlockedCredentialsAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }

    public BlockedCredentialsAuthenticationException(final String code) {
        super(code);
    }

    public BlockedCredentialsAuthenticationException(final String code,
        final Throwable throwable) {
        super(code, throwable);
        // TODO Auto-generated constructor stub
    }

}
