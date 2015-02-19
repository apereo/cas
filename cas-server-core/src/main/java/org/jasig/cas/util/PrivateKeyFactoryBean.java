/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @since 3.1
 *
 */
public final class PrivateKeyFactoryBean extends AbstractFactoryBean {

    @NotNull
    private Resource location;

    @NotNull
    private String algorithm;

    @Override
    protected Object createInstance() throws Exception {
        final InputStream privKey = this.location.getInputStream();
        try {
            final byte[] bytes = new byte[privKey.available()];
            privKey.read(bytes);
            privKey.close();
            final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(bytes);
            final KeyFactory factory = KeyFactory.getInstance(this.algorithm);
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
