/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Default implementation of {@link CredentialsToPrincipalResolver}Uses <code>SimplePrincipal</code>
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.authentication.principal.SimplePrincipal
 */
public class DefaultCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    protected final Log log = LogFactory.getLog(getClass());

    public Principal resolvePrincipal(final Credentials credentials) {
        final UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials)credentials;

        if (credentials == null) {
            throw new IllegalArgumentException("credentials cannot be null");
        }

        log.debug("Creating SimplePrincipal for ["
            + usernamePasswordCredentials.getUserName() + "]");

        return new SimplePrincipal(usernamePasswordCredentials.getUserName());
    }

    public boolean supports(Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}