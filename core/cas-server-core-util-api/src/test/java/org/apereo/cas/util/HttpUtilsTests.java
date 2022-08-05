package org.apereo.cas.util;

import lombok.val;
import org.apache.http.client.methods.CloseableHttpResponse;
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
public class HttpUtilsTests {

    @Test
    public void verifyExec() {
        val exec = HttpUtils.HttpExecutionRequest.builder()
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
    public void verifyBearerToken() {
        val exec = HttpUtils.HttpExecutionRequest.builder()
            .bearerToken(UUID.randomUUID().toString())
            .method(HttpMethod.GET)
            .entity("entity")
            .url("http://localhost:8081")
            .proxyUrl("http://localhost:8080")
            .build();

        assertNull(HttpUtils.execute(exec));
    }

    @Test
    public void verifyClose() {
        assertDoesNotThrow(() -> {
            HttpUtils.close(null);
            val response = mock(CloseableHttpResponse.class);
            doThrow(new RuntimeException()).when(response).close();
            HttpUtils.close(response);
        });
    }

    @Test
    public void verifyBadSSLLogging() {
        val exec = HttpUtils.HttpExecutionRequest.builder()
            .method(HttpMethod.GET)
            .url("https://untrusted-root.badssl.com/endpoint?secret=sensitiveinfo")
            .build();
        val response = HttpUtils.execute(exec);
        assertNotNull(response);
        assertTrue(HttpStatus.resolve(response.getStatusLine().getStatusCode()).is5xxServerError());
        assertTrue(response.getStatusLine().getReasonPhrase().contains("https://untrusted-root.badssl.com/endpoint"));
    }
}
