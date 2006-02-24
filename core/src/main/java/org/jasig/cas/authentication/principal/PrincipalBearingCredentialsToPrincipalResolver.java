/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;


/**
 * Extracts the Principal out of PrincipalBearingCredentials.
 * It is very simple to resolve PrincipalBearingCredentials to a Principal
 * since the credentials already bear the ready-to-go Principal.
 * 
 * @since 3.0.5
 * @version $Revision$ $Date$
 */
public final class PrincipalBearingCredentialsToPrincipalResolver 
    implements CredentialsToPrincipalResolver {

    public Principal resolvePrincipal(Credentials credentials) {
        return ((PrincipalBearingCredentials) credentials).getPrincipal();
    }

    public boolean supports(Credentials credentials) {
        return credentials instanceof PrincipalBearingCredentials;
    }

}