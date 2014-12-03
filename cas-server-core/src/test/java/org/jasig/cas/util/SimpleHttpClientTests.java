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

import static org.junit.Assert.*;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Test;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class SimpleHttpClientTests  {

    private SimpleHttpClient getHttpClient() {
        final SimpleHttpClient httpClient = new SimpleHttpClient();
        httpClient.setConnectionTimeout(1000);
        httpClient.setReadTimeout(1000);
        return httpClient;
    }

    @Test
    public void testOkayUrl() {
        assertTrue(this.getHttpClient().isValidEndPoint("http://www.jasig.org"));
    }

    @Test
    public void testBadUrl() {
        assertFalse(this.getHttpClient().isValidEndPoint("https://www.abc1234.org"));
    }

    @Test
    public void testInvalidHttpsUrl() {
        final HttpClient client = this.getHttpClient();
        assertFalse(client.isValidEndPoint("https://static.ak.connect.facebook.com"));
    }

    @Test
    public void testBypassedInvalidHttpsUrl() throws Exception {
        final SimpleHttpClient client = this.getHttpClient();
        client.setSSLSocketFactory(this.getFriendlyToAllSSLSocketFactory());
        client.setHostnameVerifier(this.getFriendlyToAllHostnameVerifier());
        client.setAcceptableCodes(new int[] {200, 403});
        assertTrue(client.isValidEndPoint("https://static.ak.connect.facebook.com"));
    }

    private HostnameVerifier getFriendlyToAllHostnameVerifier() {
        final HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname, final SSLSession session) { return true; }
        };
        return hv;
    }

    private SSLSocketFactory getFriendlyToAllSSLSocketFactory() throws Exception {
        final TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {}
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {}
        };
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[] {trm}, null);
        return sc.getSocketFactory();
    }
}
