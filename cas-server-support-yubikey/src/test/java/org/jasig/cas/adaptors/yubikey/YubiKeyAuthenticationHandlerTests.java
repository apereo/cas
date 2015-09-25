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

package org.jasig.cas.adaptors.yubikey;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;
    private static final String SECRET_KEY = "iBIehjui12aK8x82oe5qzGeb0As=";
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    @Test
    public void checkDefaultAccountRegistry() {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY);
        assertNull(handler.getRegistry());
    }

    @Test(expected = FailedLoginException.class)
    public void checkReplayedAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY);
        handler.authenticate(new UsernamePasswordCredential("casuser", OTP));
    }

    @Test(expected = FailedLoginException.class)
    public void checkBadConfigAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(123456, "123456");
        handler.authenticate(new UsernamePasswordCredential("casuser", OTP));
    }

    @Test(expected = AccountNotFoundException.class)
    public void checkAccountNotFound() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY, new YubiKeyAccountRegistry() {
                    @Override
                    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
                        return false;
                    }
                });

        handler.authenticate(new UsernamePasswordCredential("casuser", OTP));
    }
}


