/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package org.jasig.cas.ticket.enc;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import edu.vt.middleware.crypt.util.CryptReader;

/**
 * Factory bean that creates {@link SecretKey} objects from a file containing
 * key material.
 *
 * @author Marvin S. Addison
 * @since 4.1
 *
 */
public final class SecretKeyFactoryBean extends AbstractFactoryBean<SecretKey> {
    /** Default cipher is AES */
    public static final String DEFAULT_CIPHER = "AES";

    /** File containing key material */
    @NotNull
    private Resource keyFile;

    /** Cipher name for which key was created */
    @NotNull
    private String cipher = DEFAULT_CIPHER;

    /** {@inheritDoc} */
    protected SecretKey createInstance() throws Exception {
        return CryptReader.readSecretKey(this.keyFile.getInputStream(), this.cipher);
    }

    /** {@inheritDoc} */
    public Class<?> getObjectType() {
        return SecretKey.class;
    }

    /**
     * @param file File containing key material.
     */
    public void setKeyFile(final Resource file) {
        this.keyFile = file;
    }

    /**
     * @param cipher Cipher name for which key was created.
     */
    public void setCipher(final String cipher) {
        this.cipher = cipher;
    }
}
