/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.AuthenticationRequest;

/**
 * Strategy interface to resolve <code>Principal</code> s
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.authentication.principal.Principal
 */
public interface CredentialsToPrincipalResolver {

    /**
     * Resolve Principal for a given AuthenticationRequest
     * 
     * @param authenticationRequest from which to resolve Principal
     * @return resolved Principal
     */
    Principal resolvePrincipal(AuthenticationRequest authenticationRequest);

    boolean supports(AuthenticationRequest request);
}