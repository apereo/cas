/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredentials.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler extends
    AbstractAuthenticationHandler implements InitializingBean {

    protected final boolean authenticateInternal(final Credentials credentials)
        throws AuthenticationException {
        return authenticateUsernamePasswordInternal((UsernamePasswordCredentials) credentials);
    }

    protected abstract boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials)
        throws AuthenticationException;

    protected final boolean supports(final Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
