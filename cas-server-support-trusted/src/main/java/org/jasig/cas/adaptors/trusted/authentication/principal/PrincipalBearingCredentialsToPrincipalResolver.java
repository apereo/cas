/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Extracts the Principal out of PrincipalBearingCredentials. It is very simple
 * to resolve PrincipalBearingCredentials to a Principal since the credentials
 * already bear the ready-to-go Principal.
 * 
 * @author Andrew Petro
 * @version $Revision$ $Date: 2007-06-10 09:17:55 -0400 (Sun, 10 Jun
 * 2007) $
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsToPrincipalResolver extends
    AbstractPersonDirectoryCredentialsToPrincipalResolver {

    protected String extractPrincipalId(Credentials credentials) {
        return ((PrincipalBearingCredentials) credentials).getPrincipal()
            .getId();
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && credentials.getClass().equals(PrincipalBearingCredentials.class);
    }
}