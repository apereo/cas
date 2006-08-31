/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

/**
 * Implementation of a CredentialToPrincipalResolver that 
 * takes a SpnegoCredentials and returns a
 * SimplePrincipal.
 * 
 * @author Arnaud Lessueur
 * @author Marc-Antoine Garrigue
 * @version $Id$
 * @since 3.1
 */
public class SpnegoCredentialsToPrincipalResolver implements CredentialsToPrincipalResolver {

    private Log log = LogFactory.getLog(this.getClass());

    public Principal resolvePrincipal(Credentials credentials) {
        final SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;
        final String name = spnegoCredentials.getPrincipal().getId();

        if (log.isDebugEnabled()) {
            log.debug("Creating SimplePrincipal for [" + name + "]");
        }

        return new SimplePrincipal(name);
    }

    public boolean supports(Credentials credentials) {
        return credentials != null && SpnegoCredentials.class.equals(credentials.getClass());
    }

}
