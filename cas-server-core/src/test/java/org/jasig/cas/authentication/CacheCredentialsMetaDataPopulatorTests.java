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

import org.jasig.cas.TestUtils;
import org.jasig.cas.util.CompressionUtils;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.*;

/**
 * Tests for {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CacheCredentialsMetaDataPopulatorTests {

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Before
    public void setup() {
        final KeyPair pair = generateKeyPair();
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();
    }

    @Test
    public void verifyPasswordAsAuthenticationAttribute() {
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator(this.publicKey);

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationBuilder builder = AuthenticationBuilder.newInstance(TestUtils.getAuthentication());
        populator.populateAttributes(builder, c);
        final Authentication authn = builder.build();
        assertTrue(authn.getAttributes().containsKey(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertFalse(authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD).equals(c.getPassword()));
    }

    @Test
    public void verifyPasswordAsAuthenticationAttributeCanDecrypt() {
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator(this.publicKey);

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationBuilder builder = AuthenticationBuilder.newInstance(TestUtils.getAuthentication());
        populator.populateAttributes(builder, c);
        final Authentication authn = builder.build();
        final String psw = (String) authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);
        final String decryptedPsw = decryptCredential(psw);
        assertTrue(c.getPassword().equals(decryptedPsw));
    }

    private static KeyPair generateKeyPair() {
        try {
            final KeyPairGenerator kpg = KeyPairGenerator.getInstance(CacheCredentialsMetaDataPopulator.DEFAULT_CIPHER_ALGORITHM);
            kpg.initialize(2048);
            return kpg.genKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String decryptCredential(final String cred) {
        try {
            final Cipher cipher = Cipher.getInstance(CacheCredentialsMetaDataPopulator.DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            final byte[] cred64 = CompressionUtils.decodeBase64ToByteArray(cred);
            final byte[] cipherData = cipher.doFinal(cred64);
            return new String(cipherData);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
