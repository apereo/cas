/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.util.Iterator;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.util.LdapUtils;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class FastBindLdapAuthenticationHandler extends AbstractLdapAuthenticationHandler {
	
	/**
	 * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.domain.AuthenticationRequest)
	 */
	public boolean authenticate(final AuthenticationRequest request) {
		final UsernamePasswordAuthenticationRequest uRequest = (UsernamePasswordAuthenticationRequest) request;
		
		for (Iterator iter = this.getServers().iterator(); iter.hasNext();) {
			DirContext dirContext = null;
			final String url = (String) iter.next();

			try {
				dirContext = this.getContext(LdapUtils.getFilterWithValues(this.getFilter(), uRequest.getUserName()), uRequest.getPassword(), url);
				dirContext.close();
				return true;
			}
			catch (NamingException e) {
				// could not connect therefore not a valid user
			}
		}
		
		return false;
	}
}
