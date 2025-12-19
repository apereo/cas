package org.apereo.cas.util.http;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.HttpMessage;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link SimpleHttpClient}.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Tag("Web")
class SimpleHttpClientTests {
    private static SimpleHttpClient getHttpClient() {
        return new SimpleHttpClientFactoryBean().getObject();
    }

    private static SSLConnectionSocketFactory getFriendlyToAllSSLSocketFactory() throws Exception {
        val trm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        val sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{trm}, null);
        return new SSLConnectionSocketFactory(sc, new NoopHostnameVerifier());
    }

    @Nested
    class DefaultTests {
        @Test
        void verifyMessageRejected() {
            val msg = mock(HttpMessage.class);
            when(msg.getUrl()).thenThrow(RejectedExecutionException.class);
            val result = getHttpClient().sendMessageToEndPoint(msg);
            assertFalse(result);
        }

        @Test
        void verifyMessageFail() {
            val msg = mock(HttpMessage.class);
            when(msg.getUrl()).thenThrow(RuntimeException.class);
            val result = getHttpClient().sendMessageToEndPoint(msg);
            assertFalse(result);
        }

        @Test
        void verifyMessageSent() throws Throwable {
            try (val webServer = new MockWebServer(8165,
                new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
                webServer.start();
                val result = getHttpClient().sendMessageToEndPoint(new URI("http://localhost:8165").toURL());
                assertNotNull(result);
            }
        }

        @Test
        void verifyMessageRefused() throws Throwable {
            try (val webServer = new MockWebServer(8166,
                new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
                webServer.start();
                val result = getHttpClient().sendMessageToEndPoint(new URI("http://localhost:8166").toURL());
                assertNull(result);
            }
        }

        @Test
        void verifyMessageNotSent() throws Throwable {
            val result = getHttpClient().sendMessageToEndPoint(new URI("http://localhost:1234").toURL());
            assertNull(result);
        }

        @Test
        void verifyBadUrl() {
            assertFalse(getHttpClient().isValidEndPoint("https://www.whateverabc1234.org"));
        }

        @Test
        void verifyInvalidHttpsUrl() {
            val client = getHttpClient();
            assertFalse(client.isValidEndPoint("https://wrong.host.badssl.com/"));
            assertFalse(client.isValidEndPoint("xyz"));
        }

        @Test
        void verifyBypassedInvalidHttpsUrl() throws Throwable {
            val clientFactory = new SimpleHttpClientFactoryBean();
            clientFactory.setSslSocketFactory(getFriendlyToAllSSLSocketFactory());
            clientFactory.setHostnameVerifier(new NoopHostnameVerifier());
            clientFactory.setAcceptableCodes(CollectionUtils.wrapList(200, 403));
            val client = clientFactory.getObject();
            assertNotNull(client);
            assertTrue(client.isValidEndPoint("https://wrong.host.badssl.com/"));
        }

        @Test
        void verifyOkayUrl() {
            assertTrue(getHttpClient().isValidEndPoint("https://www.google.com"));
        }

        @Test
        void verifyValidRejected() throws Throwable {
            val port = RandomUtils.nextInt(7000, 9999);
            try (val webServer = new MockWebServer(port,
                new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
                webServer.start();
                val result = getHttpClient().isValidEndPoint(new URI("http://localhost:8099").toURL());
                assertFalse(result);
            }
        }

    }

    @Nested
    @EnabledIfListeningOnPort(port = 9859)
    class HttpBinTests {
        @Test
        void verifyMessage() throws Throwable {
            val clientFactory = new SimpleHttpClientFactoryBean();
            clientFactory.setSslSocketFactory(getFriendlyToAllSSLSocketFactory());
            clientFactory.setHostnameVerifier(new NoopHostnameVerifier());
            clientFactory.setAcceptableCodes(CollectionUtils.wrapList(200, 403));
            val client = clientFactory.getObject();
            val msg = new HttpMessage(new URI("https://localhost:9859/post").toURL(), "{'name' : 'value'}", false);
            val result = client.sendMessageToEndPoint(msg);
            assertTrue(result);
        }
    }
}
