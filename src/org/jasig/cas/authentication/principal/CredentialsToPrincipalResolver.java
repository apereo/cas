/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Strategy interface to resolve <code>Principal</code> s
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.authentication.principal.Principal
 * @see org.jasig.cas.authentication.principal.Credentials
 */
public interface CredentialsToPrincipalResolver {

    /**
     * Resolve Principal for a given Credentials
     * 
     * @param credentials from which to resolve Principal
     * @return resolved Principal
     */
    Principal resolvePrincipal(Credentials credentials);

    /**
     * Determine if a credentials type is supported by this resolver. This is checked before calling resolve principal.
     * 
     * @param credentials
     * @return true if we support these credentials, false otherwise.
     */
    boolean supports(Credentials credentials);
}