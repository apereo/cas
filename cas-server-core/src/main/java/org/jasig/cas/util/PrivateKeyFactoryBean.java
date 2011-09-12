/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;

/**
 * Factory Bean for creating a private key from a file.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public final class PrivateKeyFactoryBean extends AbstractFactoryBean {

    @NotNull
    private Resource location;
    
    @NotNull
    private String algorithm;

    protected Object createInstance() throws Exception {
        final InputStream privKey = this.location.getInputStream();
        try {
            final byte[] bytes = new byte[privKey.available()];
            privKey.read(bytes);
            privKey.close();
            final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory factory = KeyFactory.getInstance(this.algorithm);
            return factory.generatePrivate(privSpec);
        } finally {
            privKey.close();
        }
    }

    public Class getObjectType() {
        return PrivateKey.class;
    }

    public void setLocation(final Resource location) {
        this.location = location;
    }
    
    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }
}
