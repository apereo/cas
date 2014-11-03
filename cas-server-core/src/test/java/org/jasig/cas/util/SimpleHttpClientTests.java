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

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class SimpleHttpClientTests  {

    private SimpleHttpClient getHttpClient() {
        final SimpleHttpClient httpClient = new SimpleHttpClient();
        return httpClient;
    }

    @Test
    public void testOkayUrl() {
        assertTrue(this.getHttpClient().isValidEndPoint("http://www.google.com"));
    }

    @Test
    public void testBadUrl() {
        assertFalse(this.getHttpClient().isValidEndPoint("http://www.apereo.org/scottb.html"));
    }

    @Test
    public void testInvalidHttpsUrl() {
        final HttpClient client = this.getHttpClient();
        assertFalse(client.isValidEndPoint("https://static.ak.connect.facebook.com"));
    }

    @Test
    public void testBypassedInvalidHttpsUrl() throws Exception {
        final SimpleHttpClient client = new SimpleHttpClient(getFriendlyToAllSSLSocketFactory(),
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER, new int[] {200, 403});
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
