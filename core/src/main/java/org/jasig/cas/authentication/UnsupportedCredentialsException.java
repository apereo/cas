/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * The exception thrown when a Handler does not know how to determine the
 * validity of the credentials based on the fact that it does not know what to
 * do with the credentials presented.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UnsupportedCredentialsException extends AuthenticationException {

    private static final long serialVersionUID = 3977861752513837361L;

    private static final String CODE = "error.authentication.credentials.unsupported";

    /**
     * 
     */
    public UnsupportedCredentialsException() {
        super();
    }

    public String getCode() {
        return CODE;
    }
}
