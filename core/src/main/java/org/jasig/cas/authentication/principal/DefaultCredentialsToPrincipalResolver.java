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
 * The userid and password were already validated by the Handler. Extract the
 * userid and make it the ID of a SimplePrincipal object.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * 
 * @see org.jasig.cas.authentication.principal.SimplePrincipal
 */
public final class DefaultCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    /** Logging instance. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Create a SimplePrincipal containing the Userid.
     * 
     * @param credentials the Username and Password provided as credentials.
     * @return an instance of the principal where the id is the username.
     */
    public Principal resolvePrincipal(final Credentials credentials) {
        final UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;

        if (credentials == null) {
            throw new IllegalArgumentException("credentials cannot be null");
        }

        log.debug("Creating SimplePrincipal for ["
            + usernamePasswordCredentials.getUsername() + "]");

        return new SimplePrincipal(usernamePasswordCredentials.getUsername());
    }

    /**
     * Return true if Credentials are UsernamePasswordCredentials.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
