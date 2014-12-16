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
package org.jasig.cas.authentication.handler;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public final class DefaultPasswordEncoderTests {

    private final PasswordEncoder passwordEncoder = new DefaultPasswordEncoder("MD5");

    @Test
    public void verifyNullPassword() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    @Test
    public void verifyMd5Hash() {
        assertEquals("1f3870be274f6c49b3e31a0c6728957f", this.passwordEncoder
            .encode("apple"));
    }

    @Test
    public void verifySha1Hash() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("SHA1");

        final String hash = pe.encode("this is a test");

        assertEquals("fa26be19de6bff93f70bc2308434e4a440bbad02", hash);

    }

    @Test
    public void verifySha1Hash2() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("SHA1");

        final String hash = pe.encode("TEST of the SYSTEM");

        assertEquals("82ae28dfad565dd9882b94498a271caa29025d5f", hash);

    }

    @Test
    public void verifyInvalidEncodingType() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("scott");
        try {
            pe.encode("test");
            fail("exception expected.");
        } catch (final Exception e) {
            return;
        }
    }
}
