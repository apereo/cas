/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
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
 * Implementation of a password handler that encrypts a password using DES. Requires a key to have been created already and stored as a String.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DesPasswordTranslator implements PasswordTranslator, InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    private static final String ALGORITHM = "DES";

    private String key;

    private Cipher cipher;

    private BASE64Encoder encoder = new BASE64Encoder();

    public String translate(final String password) {
        try {
            byte[] passwordAsBytes = password.getBytes("UTF8");
            byte[] encryptedAsBytes = this.cipher.doFinal(passwordAsBytes);

            return this.encoder.encode(encryptedAsBytes);
        }
        catch (Exception e) {
            log.debug(e);
        }

        return null;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.key == null)
            throw new IllegalStateException("key must be set on " + this.getClass().getName());

        SecretKey secretKey = new SecretKeySpec(this.key.getBytes("UTF8"), ALGORITHM);
        this.cipher = Cipher.getInstance(ALGORITHM);
        this.cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    /**
     * @param key The key to set.
     */
    public void setKey(String key) {
        this.key = key;
    }
}