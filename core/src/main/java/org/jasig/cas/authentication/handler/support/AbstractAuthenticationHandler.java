/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Abstract handler that manages the flow of validating credentials by first
 * checking if these credentials can be checked and then delegating to a method
 * that can determine the validity of the credentials. Classes that extend this
 * class are required to implement authenticateInternal and supports.
 * 
 * @author Scott Battaglia
 * @version $Id: AbstractAuthenticationHandler.java,v 1.2 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public abstract class AbstractAuthenticationHandler implements
    AuthenticationHandler {

    public final boolean authenticate(Credentials credentials)
        throws AuthenticationException {
        if (!this.supports(credentials)) {
            throw new UnsupportedCredentialsException();
        }

        return this.authenticateInternal(credentials);
    }

    /**
     * Method to determine whether the credentials presented can be checked by
     * this handler.
     * 
     * @param credentials The credentials we want to check if the handler
     * supports.
     * @return true if the handler supports authenticating this type of
     * credentials. False otherwise.
     */
    protected abstract boolean supports(Credentials credentials);

    /**
     * Template (abstract method) that inheriting classes should implement. This
     * gets called after the check to see if the credentials are supported. This
     * method will only be invoked with credentials such that
     * (supports(credentials) == true).
     * 
     * @param credentials The credentials to check
     * @return true if the credentials are valid, false if they are not
     * @throws AuthenticationException if the validity of the credentials cannot
     * be determined.
     */
    protected abstract boolean authenticateInternal(Credentials credentials)
        throws AuthenticationException;
}