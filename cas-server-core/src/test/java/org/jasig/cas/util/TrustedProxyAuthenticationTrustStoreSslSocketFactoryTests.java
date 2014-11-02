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

import org.jasig.cas.authentication.FileTrustStoreSslSocketFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertTrue;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class TrustedProxyAuthenticationTrustStoreSslSocketFactoryTests {
    private SimpleHttpClient client;

    private static final ClassPathResource TRUST_STORE = new ClassPathResource("truststore.jks");
    private static final String TRUST_STORE_PSW = "changeit";

    @Before
    public void prepareHttpClient() throws Exception {
        final FileTrustStoreSslSocketFactory f = new FileTrustStoreSslSocketFactory(
                TRUST_STORE.getFile(), TRUST_STORE_PSW);

        this.client = new SimpleHttpClient(f);
    }

    @Ignore
    public void testSuccessfulConnection() {
        final boolean valid = client.isValidEndPoint("https://www.github.com");
        assertTrue(valid);
    }

    @Test
    public void testSuccessfulConnectionWithCustomSSLCert() {
        final boolean valid = client.isValidEndPoint("https://www.cacert.org");
        assertTrue(valid);
    }

}
