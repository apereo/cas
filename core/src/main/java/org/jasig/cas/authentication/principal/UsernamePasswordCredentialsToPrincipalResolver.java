/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of CredentialsToPrincipalResolver for Credentials based on
 * UsernamePasswordCredentials when a SimplePrincipal (username only) is
 * sufficient.
 * <p>
 * Implementation extracts the username from the Credentials provided and
 * constructs a new SimplePrincipal with the unique id set to the username.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.authentication.principal.SimplePrincipal
 */
public final class UsernamePasswordCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    /** Logging instance. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Constructs a SimplePrincipal from the username provided in the
     * credentials.
     * 
     * @param credentials the Username and Password provided as credentials.
     * @return an instance of the principal where the id is the username.
     */
    public Principal resolvePrincipal(final Credentials credentials) {
        final UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;

        if (log.isDebugEnabled()) {
            log.debug("Creating SimplePrincipal for ["
                + usernamePasswordCredentials.getUsername() + "]");
        }

        return new SimplePrincipal(usernamePasswordCredentials.getUsername());
    }

    /**
     * Return true if Credentials are UsernamePasswordCredentials, false
     * otherwise.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
