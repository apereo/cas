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
public class DefaultCredentialsToPrincipalResolver implements CredentialsToPrincipalResolver {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver#resolvePrincipal(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public Principal resolvePrincipal(final Credentials credentials) {
        final UsernamePasswordCredentials basicAuthenticationRequest = (UsernamePasswordCredentials)credentials;
        log.debug("Creating SimplePrincipal for [" + basicAuthenticationRequest.getUserName() + "]");
        return new SimplePrincipal(basicAuthenticationRequest.getUserName());
    }

    /**
     * @see org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver#supports(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean supports(Credentials request) {
        return UsernamePasswordCredentials.class.isAssignableFrom(request.getClass());
    }
}