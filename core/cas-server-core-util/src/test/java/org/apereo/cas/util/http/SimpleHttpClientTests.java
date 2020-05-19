package org.apereo.cas.util.http;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link SimpleHttpClient}.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Tag("Simple")
public class SimpleHttpClientTests {

    private static SimpleHttpClient getHttpClient() {
        return new SimpleHttpClientFactoryBean().getObject();
    }

    @SneakyThrows
    private static SSLConnectionSocketFactory getFriendlyToAllSSLSocketFactory() {
        val trm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
            }
        };
        val sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{trm}, null);
        return new SSLConnectionSocketFactory(sc, new NoopHostnameVerifier());
    }

    @Test
    public void verifyOkayUrl() {
        assertTrue(getHttpClient().isValidEndPoint("https://www.google.com"));
    }

    @Test
    public void verifyValidRejected() throws Exception {
        try (val webServer = new MockWebServer(8099,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val result = getHttpClient().isValidEndPoint(new URL("http://localhost:8099"));
            assertFalse(result);
        }
    }

    @Test
    public void verifyMessage() throws Exception {
        val msg = new HttpMessage(new URL("https://httpbin.org/post"), "{'name' : 'value'}", false);
        val result = getHttpClient().sendMessageToEndPoint(msg);
        assertTrue(result);
    }

    @Test
    public void verifyMessageRejected() {
        val msg = mock(HttpMessage.class);
        when(msg.getUrl()).thenThrow(RejectedExecutionException.class);
        val result = getHttpClient().sendMessageToEndPoint(msg);
        assertFalse(result);
    }

    @Test
    public void verifyMessageFail() {
        val msg = mock(HttpMessage.class);
        when(msg.getUrl()).thenThrow(RuntimeException.class);
        val result = getHttpClient().sendMessageToEndPoint(msg);
        assertFalse(result);
    }

    @Test
    public void verifyMessageSent() throws Exception {
        try (val webServer = new MockWebServer(8165,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val result = getHttpClient().sendMessageToEndPoint(new URL("http://localhost:8165"));
            assertNotNull(result);
        }
    }

    @Test
    public void verifyMessageRefused() throws Exception {
        try (val webServer = new MockWebServer(8166,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val result = getHttpClient().sendMessageToEndPoint(new URL("http://localhost:8166"));
            assertNull(result);
        }
    }

    @Test
    public void verifyMessageNotSent() throws Exception {
        val result = getHttpClient().sendMessageToEndPoint(new URL("http://localhost:1234"));
        assertNull(result);
    }

    @Test
    public void verifyBadUrl() {
        assertFalse(getHttpClient().isValidEndPoint("https://www.whateverabc1234.org"));
    }

    @Test
    public void verifyInvalidHttpsUrl() {
        val client = getHttpClient();
        assertFalse(client.isValidEndPoint("https://wrong.host.badssl.com/"));
        assertFalse(client.isValidEndPoint("xyz"));
    }

    @Test
    public void verifyBypassedInvalidHttpsUrl() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(getFriendlyToAllSSLSocketFactory());
        clientFactory.setHostnameVerifier(new NoopHostnameVerifier());
        clientFactory.setAcceptableCodes(CollectionUtils.wrapList(200, 403));
        val client = clientFactory.getObject();
        assertNotNull(client);
        assertTrue(client.isValidEndPoint("https://wrong.host.badssl.com/"));
    }
}
