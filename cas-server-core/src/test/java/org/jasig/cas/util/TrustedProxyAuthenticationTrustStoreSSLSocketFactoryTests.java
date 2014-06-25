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
package org.jasig.cas.util;

import static org.junit.Assert.assertTrue;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Misagh Moayyed
 * @since
 */
public class TrustedProxyAuthenticationTrustStoreSSLSocketFactoryTests {
    private SimpleHttpClient client;

    private static final ClassPathResource TRUST_STORE = new ClassPathResource("truststore.jks");
    private static final String TRUST_STORE_PSW = "changeit";

    @Before
    public void prepareHttpClient() throws Exception {
        this.client = new SimpleHttpClient();
        client.initializeHttpClient();

        final TrustedProxyAuthenticationTrustStoreSSLSocketFactory f = new TrustedProxyAuthenticationTrustStoreSSLSocketFactory(
                TRUST_STORE.getFile(), TRUST_STORE_PSW);
        final SSLConnectionSocketFactory fact = f.createInstance();

        this.client.setSSLSocketFactory(fact);
    }

    @Test
    public void testSuccessfulConnection() {
        final boolean valid = client.isValidEndPoint("https://www.github.com");
        assertTrue(valid);
    }

    @Test
    public void testSuccessfulConnectionWithCustomSSLCertMismatch() {
        final boolean valid = client.isValidEndPoint("https://tv.eurosport.com/");
        assertTrue(valid);
    }
    
    @Test
    public void testSuccessfulConnectionWithCustomSSLCert() {
        final boolean valid = client.isValidEndPoint("https://testssl-expire.disig.sk/index.en.html");
        assertTrue(valid);
    }

}
