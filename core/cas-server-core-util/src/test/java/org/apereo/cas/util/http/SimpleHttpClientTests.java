package org.apereo.cas.util.http;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import static org.junit.Assert.*;

/**
 * Test cases for {@link SimpleHttpClient}.
 * @author Scott Battaglia
 * @since 3.1
 */
public class SimpleHttpClientTests {

    private static SimpleHttpClient getHttpClient() throws Exception {
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
        assertFalse(client.isValidEndPoint("https://wrong.host.badssl.com/"));
    }

    @Test
    public void verifyBypassedInvalidHttpsUrl() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(getFriendlyToAllSSLSocketFactory());
        clientFactory.setHostnameVerifier(new NoopHostnameVerifier());
        clientFactory.setAcceptableCodes(new int[] {200, 403});
        final SimpleHttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://wrong.host.badssl.com/"));
    }

    private static SSLConnectionSocketFactory getFriendlyToAllSSLSocketFactory() throws Exception {
        final TrustManager trm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {}
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {}
        };
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[] {trm}, null);
        return new SSLConnectionSocketFactory(sc, new NoopHostnameVerifier());
    }
}
