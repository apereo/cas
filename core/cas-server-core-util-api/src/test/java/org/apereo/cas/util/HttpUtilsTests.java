package org.apereo.cas.util;

import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import lombok.val;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HttpUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
class HttpUtilsTests {

    @Test
    void verifyRetryOnErrors() {
        try (val webServer = new MockWebServer(HttpStatus.BAD_REQUEST)) {
            webServer.start();
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword("password")
                .basicAuthUsername("user")
                .method(HttpMethod.GET)
                .entity("entity")
                .url("http://localhost:%s".formatted(webServer.getPort()))
                .build();
            assertNotNull(HttpUtils.execute(exec));
        }
    }

    @Test
    void verifyExecWithExistingClient() {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword("password")
                .basicAuthUsername("user")
                .method(HttpMethod.GET)
                .entity("entity")
                .url("http://localhost:%s".formatted(webServer.getPort()))
                .httpClient(new SimpleHttpClientFactoryBean().getObject())
                .build();
            assertNotNull(HttpUtils.execute(exec));
        }
    }

    @Test
    void verifyExec() {
        val exec = HttpExecutionRequest.builder()
            .basicAuthPassword("password")
            .basicAuthUsername("user")
            .method(HttpMethod.GET)
            .entity("entity")
            .url("http://localhost:8081")
            .proxyUrl("http://localhost:8080")
            .build();
        assertNull(HttpUtils.execute(exec));
    }

    @Test
    void verifyBearerToken() {
        val exec = HttpExecutionRequest.builder()
            .bearerToken(UUID.randomUUID().toString())
            .method(HttpMethod.GET)
            .entity("entity")
            .url("http://localhost:8081")
            .proxyUrl("http://localhost:8080")
            .build();

        assertNull(HttpUtils.execute(exec));
    }

    @Test
    void verifyClose() {
        assertDoesNotThrow(() -> {
            HttpUtils.close(null);
            val response = mock(CloseableHttpResponse.class);
            doThrow(new RuntimeException()).when(response).close();
            HttpUtils.close(response);
        });
    }

    @Test
    void verifyBadSSLLogging() {
        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.GET)
            .url("https://untrusted-root.badssl.com/endpoint?secret=sensitiveinfo")
            .build();
        val response = HttpUtils.execute(exec);
        assertNotNull(response);

        assertTrue(HttpStatus.valueOf(response.getCode()).is5xxServerError());
        assertTrue(response.getReasonPhrase().contains("https://untrusted-root.badssl.com/endpoint"));
    }
}
