/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

public class BadCredentialsAuthenticationException extends
    AuthenticationException {

    private static final long serialVersionUID = 3256719585087797044L;

    private static final String CODE = "error.authentication.credentials.bad";

    public String getCode() {
        return CODE;
    }

}
