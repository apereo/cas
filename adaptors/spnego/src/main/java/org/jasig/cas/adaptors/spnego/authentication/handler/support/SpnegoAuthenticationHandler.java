/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.handler.support;

import org.jasig.cas.adaptors.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Implementation of an AuthenticationHandler that merely checks for the
 * existance of an established GSSContext session.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class SpnegoAuthenticationHandler implements AuthenticationHandler {

    public boolean authenticate(final Credentials credentials)
        throws AuthenticationException {
        final SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;

        return spnegoCredentials.getContext().isEstablished();
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null && SpnegoCredentials.class.equals(credentials.getClass());
    }
}
