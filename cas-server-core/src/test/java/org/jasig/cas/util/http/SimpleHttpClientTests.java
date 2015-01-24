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
package org.jasig.cas.util.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.Test;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class SimpleHttpClientTests  {

    private SimpleHttpClient getHttpClient() throws Exception {
        final SimpleHttpClient httpClient = new SimpleHttpClientFactoryBean().getObject();
        return httpClient;
    }

    @Test
    public void verifyOkayUrl() throws Exception {
        assertTrue(this.getHttpClient().isValidEndPoint("http://www.google.com"));
    }

    @Test
    public void verifyBadUrl() throws Exception {
        assertFalse(this.getHttpClient().isValidEndPoint("https://www.abc1234.org"));
    }

    @Test
    public void verifyInvalidHttpsUrl() throws Exception {
        final HttpClient client = this.getHttpClient();
        assertFalse(client.isValidEndPoint("https://static.ak.connect.facebook.com"));
    }

    @Test
    public void verifyBypassedInvalidHttpsUrl() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(getFriendlyToAllSSLSocketFactory());
        clientFactory.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        clientFactory.setAcceptableCodes(new int[] {200, 403});
        final SimpleHttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://static.ak.connect.facebook.com"));
    }

    private SSLConnectionSocketFactory getFriendlyToAllSSLSocketFactory() throws Exception {
        final TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {}
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {}
        };
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[] {trm}, null);
        return new SSLConnectionSocketFactory(sc);
    }
}
