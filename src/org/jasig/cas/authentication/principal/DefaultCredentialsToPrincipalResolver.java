/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;


/**
 * <p>Default implementation of {@link CredentialsToPrincipalResolver}  Uses <code>SimplePrincipal</code>
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.authentication.principal.SimplePrincipal
 */
public class DefaultCredentialsToPrincipalResolver implements CredentialsToPrincipalResolver {
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 
	 * @see org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver#resolvePrincipal(org.jasig.cas.authentication.AuthenticationRequest)
	 */
    public Principal resolvePrincipal(final AuthenticationRequest authenticationRequest) {
    	final UsernamePasswordAuthenticationRequest basicAuthenticationRequest = (UsernamePasswordAuthenticationRequest) authenticationRequest;
    	logger.debug("Creating SimplePrincipal for [" + basicAuthenticationRequest.getUserName() + "]");
        return new SimplePrincipal(basicAuthenticationRequest.getUserName());
    }

    /**
     * 
     * @see org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver#supports(org.jasig.cas.authentication.AuthenticationRequest)
     */
	public boolean supports(AuthenticationRequest request) {
		return request.getClass().isAssignableFrom(UsernamePasswordAuthenticationRequest.class);
	}
}