/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;


/**
 * Credentials that bear the fully resolved and authenticated Principal,
 * or an indication that there is no such Principal.
 * 
 * These Credentials are a mechanism to pass into CAS information about an
 * authentication and Principal resolution that has already happened in layers
 * in front of CAS, e.g. by means of a Java Servlet Filter or by means of
 * container authentication in the servlet container or Apache layers.
 * 
 * DO NOT accept these Credentials from arbitrary web-servicey calls to CAS.
 * Rather, the code constructing these Credentials must be trusted to perform
 * appropriate authentication before issuing these credentials.
 * 
 * @since 3.0.5
 * @version $revision:$ $date:$
 */
public final class PrincipalBearingCredentials 
    implements Credentials {

    private static final long serialVersionUID = 1L;
    
    private Principal principal;
    
    /**
     * Get the previously authenticated Principal.
     * Returns null if no Principal authenticated.
     * @return authenticated Principal or null
     */
    public Principal getPrincipal(){
        return this.principal;
    }
    
    /**
     * Set the authenticated Principal, or null if there is no authenticated
     * Principal.
     * @param principal
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PrincipalBearingCredentials bearing: ");
        sb.append(this.principal);
        return sb.toString();
    }
    
}
