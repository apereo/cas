/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Interface to provide a standard way to translate a plaintext password into a different representation
 * of that password (i.e. md5) so that the password may be compared with the stored encrypted password
 * without having to decode the encrypted password.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface PasswordTranslator {

	/**
	 * Method that actually performs the transformation of the plaintext password into the encrypted
	 * password.
	 * 
	 * @param password the password to translate
	 * @return the transformed version of the password
	 */
	public String translate(String password);
}
