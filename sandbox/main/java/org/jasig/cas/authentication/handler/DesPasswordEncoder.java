/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import sun.misc.BASE64Encoder;

/**
 * Implementation of a password handler that encrypts a password using DES.
 * Requires a key to have been created already and stored as a String.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DesPasswordEncoder implements PasswordEncoder,
    InitializingBean {

    /** Logger. */
    private final Log log = LogFactory.getLog(getClass());

    /** The algorithm to use. */
    private static final String ALGORITHM = "DES";

    /** The key for the cipher. */
    private String key;

    /** The actual cipher based on the algorithm name. */
    private Cipher cipher;

    /** The encoder to do BASE64 encoding. */
    private BASE64Encoder encoder = new BASE64Encoder();

    public String encode(final String password) {
        try {
            byte[] passwordAsBytes = password.getBytes("UTF8");
            byte[] encryptedAsBytes = this.cipher.doFinal(passwordAsBytes);

            return this.encoder.encode(encryptedAsBytes);
        } catch (Exception e) {
            log.debug(e);
        }

        return null;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.key == null) {
            throw new IllegalStateException("key must be set on "
                + this.getClass().getName());
        }

        SecretKey secretKey = new SecretKeySpec(this.key.getBytes("UTF8"),
            ALGORITHM);
        this.cipher = Cipher.getInstance(ALGORITHM);
        this.cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    /**
     * @param key The key to set.
     */
    public void setKey(final String key) {
        this.key = key;
    }
}
