/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BadCredentialsAuthenticationException extends
    AuthenticationException {
    /** serialVersionID for serializability. */
    private static final long serialVersionUID = 3256719585087797044L;

    /** String code representing this exception. */
    private static final String CODE = "error.authentication.credentials.bad";

    public String getCode() {
        return CODE;
    }

}
