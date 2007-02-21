/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractPreAndPostProcessingAuthenticationHandler implements
    AuthenticationHandler {

    protected boolean preAuthenticate(final Credentials credentials) {
        return true;
    }

    protected boolean postAuthenticate(final Credentials credentials,
        final boolean authenticated) {
        return authenticated;
    }

    public final boolean authenticate(final Credentials credentials)
        throws AuthenticationException {

        if (!preAuthenticate(credentials)) {
            return false;
        }

        final boolean authenticated = doAuthentication(credentials);

        return postAuthenticate(credentials, authenticated);
    }

    protected abstract boolean doAuthentication(final Credentials credentials) throws AuthenticationException;
}
