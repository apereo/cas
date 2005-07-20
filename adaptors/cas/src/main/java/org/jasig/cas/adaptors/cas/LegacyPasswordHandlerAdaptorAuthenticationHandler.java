/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.cas;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.beans.factory.InitializingBean;

import edu.yale.its.tp.cas.auth.PasswordHandler;

/**
 * An AuthenticationHandler that obtains the hidden HttpServletRequest 
 * bound to the Credentials to present to a Legacy CAS 2 Password Handler.
 * Then map the response back to the new interface.
 * 
 * <p>Requires a CAS 2 PasswordHandler object wired to the PasswordHandler
 * property</p>
 * 
 * <p>Only responds to LegacyCasCredentials</p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class LegacyPasswordHandlerAdaptorAuthenticationHandler implements
		AuthenticationHandler, InitializingBean {

	private PasswordHandler passwordHandler;

	/**
	 * AuthenticationHandler method
	 */
	public boolean authenticate(final Credentials credentials) {

		final LegacyCasCredentials casCredentials = (LegacyCasCredentials) credentials;

		return this.passwordHandler.authenticate(casCredentials
				.getServletRequest(), casCredentials.getUsername(),
				casCredentials.getPassword());
	}

	public boolean supports(final Credentials credentials) {
		if (credentials == null)
			return false;

		return LegacyCasCredentials.class.equals(credentials.getClass());
	}

	/**
	 * @param passwordHandler CAS 2 PasswordHandler object to be adapted
	 * to the new AuthenticationHandler interface.
	 */
	public void setPasswordHandler(final PasswordHandler passwordHandler) {
		this.passwordHandler = passwordHandler;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.passwordHandler == null) {
			throw new IllegalStateException("passwordHandler must be set on "
					+ this.getClass().getName());
		}
	}
}