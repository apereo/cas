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
 */
public interface CredentialsToPrincipalResolver {

    /**
     * Resolve Principal for a given Credentials
     * 
     * @param credentials from which to resolve Principal
     * @return resolved Principal
     */
    Principal resolvePrincipal(Credentials credentials);

    boolean supports(Credentials request);
}