/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Date;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;

/**
 * 
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 *
 */
public interface Authentication {
	/**
	 * Retrieve the credentials used to authenticate the principal.
	 * @return the credentials that were used to authenticate the principal.
	 */
	Credentials getCredentials();

	/**
	 * Retrieve the principal that was authenticated. 
	 * @return the principal that was authenticated.
	 */	
	Principal getPrincipal();
	
	/**
	 * Retrieve that date/time that the authentication occurred.
	 * @return the date/time the authentication occurred.
	 */
	Date getAuthenticatedDate();
	
	/**
	 * Retrieve any additional authentication information.
	 * @return any additional authentication information.
	 */
	Object getAttributes();
}
