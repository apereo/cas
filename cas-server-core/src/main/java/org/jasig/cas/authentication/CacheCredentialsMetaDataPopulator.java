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
package org.jasig.cas.authentication;

import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.validation.constraints.NotNull;
import java.security.PublicKey;


/**
 * We utilize the {@link org.jasig.cas.authentication.AuthenticationMetaDataPopulator} to retrieve and store
 * the password as an authentication attribute. The password is encrypted using
 * {@link #DEFAULT_CIPHER_ALGORITHM} and converted to base64 by default. To decrypt, one
 * can first decode from base64, and use a cipher of size 2048 to retrieve the original.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CacheCredentialsMetaDataPopulator implements AuthenticationMetaDataPopulator {

    /** The default algorithm to encrypt the password with. */
    public static final String DEFAULT_CIPHER_ALGORITHM = "RSA";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private final PublicKey publicKey;

    @NotNull
    private final String cipherAlgorithm;

    /**
     * Instantiates a new Cache credentials meta data populator.
     *
     * @param publicKey the public key
     */
    public CacheCredentialsMetaDataPopulator(final PublicKey publicKey) {
        this.publicKey = publicKey;
        this.cipherAlgorithm = DEFAULT_CIPHER_ALGORITHM;
    }

    /**
     * Instantiates a new Cache credentials meta data populator.
     *
     * @param publicKey the public key
     * @param cipherAlgorithm the cipher algorithm
     */
    public CacheCredentialsMetaDataPopulator(final PublicKey publicKey, final String cipherAlgorithm) {
        this.publicKey = publicKey;
        this.cipherAlgorithm = cipherAlgorithm;
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        try {
            logger.debug("Processing request to capture the credential for [{}]", credential.getId());

            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            logger.debug("Created cipher instance to encrypt credential via [{}]", this.cipherAlgorithm);

            cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
            logger.debug("Initialized cipher in encrypt-mode via the public key algorithm [{}]",
                    this.publicKey.getAlgorithm());

            final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
            final byte[] cipherData = cipher.doFinal(c.getPassword().getBytes());
            final String password = CompressionUtils.encodeBase64(cipherData);
            builder.addAttribute(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD,
                    password);
            logger.debug("Encrypted credential is encoded in base64 and added as the authentication attribute [{}]",
                    UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
