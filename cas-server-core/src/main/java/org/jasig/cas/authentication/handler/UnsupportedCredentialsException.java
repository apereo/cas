/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

/**
 * The exception thrown when a Handler does not know how to determine the
 * validity of the credentials based on the fact that it does not know what to
 * do with the credentials presented.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class UnsupportedCredentialsException extends
    AuthenticationException {

    /** Static instance of UnsupportedCredentialsException. */
    public static final UnsupportedCredentialsException ERROR = new UnsupportedCredentialsException();

    /** Unique ID for serializing. */
    private static final long serialVersionUID = 3977861752513837361L;

    /** The code description of this exception. */
    private static final String CODE = "error.authentication.credentials.unsupported";

    /**
     * Default constructor that does not allow the chaining of exceptions and
     * uses the default code as the error code for this exception.
     */
    public UnsupportedCredentialsException() {
        super(CODE);
    }

    /**
     * Constructor that allows for the chaining of exceptions. Defaults to the
     * default code provided for this exception.
     * 
     * @param throwable the chained exception.
     */
    public UnsupportedCredentialsException(final Throwable throwable) {
        super(CODE, throwable);
    }
}
