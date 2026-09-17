package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    /**
     * These two tests assert that an unreachable endpoint produces an error response, so they need
     * ports that nothing is listening on. They cannot be any port in 4000-9999: that is the range
     * {@code MockWebServer} draws from, this category runs its tests in parallel, and a sibling
     * test's mock server landing on the chosen port turns "connection refused" into a real reply
     * and the assertion inverts. Ports 8080 and 8081, which these tests used to hardcode, are also
     * the two most likely things to be running on a developer's own machine.
     */
    private static final int UNREACHABLE_PROXY_PORT = 61080;

    private static final int UNREACHABLE_TARGET_PORT = 61081;

    @Test
    void verifyPerRequestResponseSizeLimit() throws Exception {
        val body = "1234567890123456";
        try (val webServer = new MockWebServer(HttpStatus.OK, body)) {
            webServer.start();
            val execution = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost:%s".formatted(webServer.getPort()))
                .maximumResponseSize(body.length() - 1)
                .build().withoutRetry();
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpUtils.execute(execution).getCode());
            val response = HttpUtils.execute(execution.withMaximumResponseSize(body.length()));
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(body, EntityUtils.toString(((HttpEntityContainer) response).getEntity()));
            assertEquals(HttpStatus.OK.value(), HttpUtils.execute(execution.withMaximumResponseSize(0)).getCode());
            assertEquals(3, webServer.getRequestCount());
        }
    }

    @Test
    void verifyDecompressedResponseSizeLimit() throws Exception {
        val body = "metadata".repeat(128);
        val output = new ByteArrayOutputStream();
        try (val gzip = new GZIPOutputStream(output)) {
            gzip.write(body.getBytes(StandardCharsets.UTF_8));
        }
        assertTrue(output.size() < 64);
        try (val webServer = new MockWebServer(new ByteArrayResource(output.toByteArray()))) {
            webServer.headers(Map.of("Content-Encoding", "gzip"));
            webServer.start();
            val execution = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost:%s".formatted(webServer.getPort()))
                .maximumResponseSize(64)
                .build().withoutRetry();
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpUtils.execute(execution).getCode());
            val response = HttpUtils.execute(execution.withMaximumResponseSize(body.length()));
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(body, EntityUtils.toString(((HttpEntityContainer) response).getEntity()));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void verifyOversizedResponseIsNotDrained(final boolean chunked) throws Exception {
        try (val server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            server.setSoTimeout(5000);
            val disconnected = executor.submit(() -> {
                try (val socket = server.accept()) {
                    socket.setSoTimeout(5000);
                    val input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                    var line = StringUtils.EMPTY;
                    do {
                        line = input.readLine();
                    } while (StringUtils.isNotEmpty(line));
                    
                    val response = chunked
                        ? "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\n11\r\n12345678901234567\r\n"
                        : "HTTP/1.1 200 OK\r\nContent-Length: 1000000\r\n\r\n";
                    socket.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
                    socket.getOutputStream().flush();
                    return input.read();
                }
            });
            val execution = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost:%s".formatted(server.getLocalPort()))
                .maximumResponseSize(16)
                .build().withoutRetry();
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpUtils.execute(execution).getCode());
            assertEquals(-1, disconnected.get(10, TimeUnit.SECONDS));
        }
    }


    @Test
    void verifyClientReuseAcrossRequests() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val url = "http://localhost:%s".formatted(webServer.getPort());

            for (var i = 0; i < 25; i++) {
                val exec = HttpExecutionRequest.builder()
                    .method(HttpMethod.GET)
                    .url(url)
                    .build();
                val response = HttpUtils.execute(exec);
                assertNotNull(response);
                assertEquals(HttpStatus.OK.value(), response.getCode());
                HttpUtils.close(response);
            }

            val failures = new ConcurrentLinkedQueue<String>();
            val futures = new ArrayList<Future<?>>();
            try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (var i = 0; i < 20; i++) {
                    futures.add(executor.submit(() -> {
                        val exec = HttpExecutionRequest.builder()
                            .method(HttpMethod.GET)
                            .url(url)
                            .build();
                        val response = HttpUtils.execute(exec);
                        if (response == null || response.getCode() != HttpStatus.OK.value()) {
                            failures.add("Unexpected response " + response);
                        }
                        HttpUtils.close(response);
                        return null;
                    }));
                }
                for (val future : futures) {
                    future.get();
                }
            }
            assertTrue(failures.isEmpty(), () -> String.join("; ", failures));
        }
    }

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
                .build()
                .withoutRetry();
            assertNotNull(HttpUtils.execute(exec));
        }
    }

    @Test
    void verifyExecWithPinnedAddress() throws Exception {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url("http://sector.example.org:%s".formatted(webServer.getPort()))
                .resolvedAddresses(Map.of("sector.example.org", new InetAddress[]{InetAddress.getLoopbackAddress()}))
                .build()
                .withoutRetry();
            val response = HttpUtils.execute(exec);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(1, webServer.getRequestCount());
        }
    }

    @Test
    void verifyExecWithoutRedirects() {
        try (val targetServer = new MockWebServer(HttpStatus.OK);
             val redirectServer = new MockWebServer(HttpStatus.FOUND)) {
            targetServer.start();
            redirectServer.headers(Map.of("Location", "http://localhost:%s".formatted(targetServer.getPort())));
            redirectServer.start();

            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost:%s".formatted(redirectServer.getPort()))
                .redirectsEnabled(false)
                .build()
                .withoutRetry();
            val response = HttpUtils.execute(exec);
            assertEquals(HttpStatus.FOUND.value(), response.getCode());
            assertEquals(1, redirectServer.getRequestCount());
            assertEquals(0, targetServer.getRequestCount());
        }
    }

    @Test
    void verifyExec() {
        val exec = HttpExecutionRequest.builder()
            .basicAuthPassword("password")
            .basicAuthUsername("user")
            .method(HttpMethod.GET)
            .entity("entity")
            .url("http://localhost:%s".formatted(UNREACHABLE_TARGET_PORT))
            .proxyUrl("http://localhost:%s".formatted(UNREACHABLE_PROXY_PORT))
            .build()
            .withoutRetry();
        val result = HttpUtils.execute(exec);
        assertNotNull(result);
        assertTrue(HttpStatus.resolve(result.getCode()).isError());
    }

    @Test
    void verifyBearerToken() {
        val exec = HttpExecutionRequest.builder()
            .bearerToken(UUID.randomUUID().toString())
            .method(HttpMethod.GET)
            .entity("entity")
            .url("http://localhost:%s".formatted(UNREACHABLE_TARGET_PORT))
            .proxyUrl("http://localhost:%s".formatted(UNREACHABLE_PROXY_PORT))
            .build()
            .withoutRetry();

        val result = HttpUtils.execute(exec);
        assertNotNull(result);
        assertTrue(HttpStatus.resolve(result.getCode()).isError());
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
            .build()
            .withoutRetry();
        val response = HttpUtils.execute(exec);
        assertNotNull(response);

        assertTrue(HttpStatus.valueOf(response.getCode()).is5xxServerError());
        assertTrue(response.getReasonPhrase().contains("https://untrusted-root.badssl.com/endpoint"));
    }
}
