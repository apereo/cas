/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util.support;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.util.PasswordTranslator;
import org.springframework.beans.factory.InitializingBean;

import sun.misc.BASE64Encoder;


/**
 * Implementation of a password handler that encrypts a password using DES.  Requires
 * a key to have been created already and stored as a String.
 * 
 * @author Scott Battaglia
 * @version $Id$
 * 
 */
public class DESPasswordTranslator implements PasswordTranslator, InitializingBean {
	protected final Log logger = LogFactory.getLog(getClass());
	private static final String ALGORITHM = "DES";
	private SecretKey secretKey;
	private String key;
	private Cipher cipher;
	private BASE64Encoder encoder = new BASE64Encoder();
	/**
	 * @see org.jasig.cas.util.PasswordTranslator#translate(java.lang.String)
	 */
	public String translate(String password) {
		try {
			byte[] passwordAsBytes = password.getBytes("UTF8");
			byte[] encryptedAsBytes = cipher.doFinal(passwordAsBytes);
			
			return encoder.encode(encryptedAsBytes);
		} catch (Exception e) {
			logger.debug(e);
		}

		return null;
	}
	
	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (key == null)
			throw new IllegalStateException("key must be set on " + this.getClass().getName());
		
		SecretKey secretKey = new SecretKeySpec(key.getBytes("UTF8"), ALGORITHM);
		cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	}

	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}
}
