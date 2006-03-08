/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a GSSName and
 * returns a SimplePrincipal by toStringing the GSSName.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class SpnegoCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    private Log log = LogFactory.getLog(this.getClass());

    public Principal resolvePrincipal(final Credentials credentials) {
        final SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;
        final GSSContext context = spnegoCredentials.getContext();

        try {
            final GSSName name = context.getSrcName();

            if (log.isDebugEnabled()) {
                log.debug("Converted GSSContext to Simple Principal of name: "
                    + name.toString());
            }

            return new SimplePrincipal(name.toString());
        } catch (final GSSException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean supports(final Credentials credentials) {
        return SpnegoCredentials.class.equals(credentials.getClass());
    }
}
