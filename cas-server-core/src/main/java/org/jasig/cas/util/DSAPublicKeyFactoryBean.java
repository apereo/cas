/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.jasig.cas.util.annotation.NotNull;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean for creating a public key from a file.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class DSAPublicKeyFactoryBean extends AbstractFactoryBean {

    @NotNull
    private Resource resource;

    @Override
    protected Object createInstance() throws Exception {
        final InputStream pubKey = this.resource.getInputStream();
        try {
            final byte[] bytes = new byte[pubKey.available()];
            pubKey.read(bytes);
            pubKey.close();
            final X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(bytes);
            final KeyFactory factory = KeyFactory.getInstance("DSA");
            return factory.generatePublic(pubSpec);
        } finally {
            pubKey.close();
        }
    }

    public Class getObjectType() {
        return DSAPublicKey.class;
    }

    public void setLocation(final Resource resource) {
        this.resource = resource;
    }
}
