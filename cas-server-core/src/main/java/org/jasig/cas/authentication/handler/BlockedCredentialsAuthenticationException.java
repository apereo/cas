/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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

    /** Static instance of BlockedCredentialsAuthenticationException. */
    public static final BlockedCredentialsAuthenticationException ERROR = new BlockedCredentialsAuthenticationException();

    /** Unique ID for serialization. */
    private static final long serialVersionUID = 3544669598642420017L;

    /** The default code for this exception used for message resolving. */
    private static final String CODE = "error.authentication.credentials.blocked";

    /**
     * Default constructor that does not allow the chaining of exceptions and
     * uses the default code as the error code for this exception.
     */
    public BlockedCredentialsAuthenticationException() {
        super(CODE);
    }

    /**
     * Constructor that allows for the chaining of exceptions. Defaults to the
     * default code provided for this exception.
     * 
     * @param throwable the chained exception.
     */
    public BlockedCredentialsAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }

    /**
     * Constructor that allows for providing a custom error code for this class.
     * Error codes are often used to resolve exceptions into messages. Providing
     * a custom error code allows the use of a different message.
     * 
     * @param code the custom code to use with this exception.
     */
    public BlockedCredentialsAuthenticationException(final String code) {
        super(code);
    }

    /**
     * Constructor that allows for chaining of exceptions and a custom error
     * code.
     * 
     * @param code the custom error code to use in message resolving.
     * @param throwable the chained exception.
     */
    public BlockedCredentialsAuthenticationException(final String code,
        final Throwable throwable) {
        super(code, throwable);
    }
}
