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
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertTrue;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class TrustedProxyAuthenticationTrustStoreSslSocketFactoryTests {
    private static final ClassPathResource TRUST_STORE = new ClassPathResource("truststore.jks");
    private static final String TRUST_STORE_PSW = "changeit";

    private HttpClient client;

    @Before
    public void prepareHttpClient() throws Exception {
        final FileTrustStoreSslSocketFactory sslFactory = new FileTrustStoreSslSocketFactory(
                TRUST_STORE.getFile(), TRUST_STORE_PSW);

        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory);
        this.client = clientFactory.getObject();
    }

    @Test
    public void verifySuccessfulConnection() {
        final boolean valid = client.isValidEndPoint("https://www.github.com");
        assertTrue(valid);
    }

    @Test
    public void verifySuccessfulConnectionWithCustomSSLCert() {
        final boolean valid = client.isValidEndPoint("https://self-signed.badssl.com");
        assertTrue(valid);
    }

}
