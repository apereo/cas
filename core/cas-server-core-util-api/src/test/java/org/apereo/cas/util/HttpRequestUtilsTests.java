package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.val;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HttpRequestUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
class HttpRequestUtilsTests {

    @Test
    void verifyResponseDeclaredSizeLimit() throws Exception {
        val entity = mock(HttpEntity.class);
        when(entity.getContentLength()).thenReturn(17L);
        try (val response = new BasicClassicHttpResponse(200)) {
            response.setEntity(entity);
            assertThrows(IOException.class, () -> HttpRequestUtils.responseHandler(16).handleResponse(response));
            verify(entity, never()).getContent();
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 8})
    void verifyResponseStreamedSizeLimit(final long declaredLength) throws Exception {
        try (val input = new ByteArrayInputStream(new byte[32]);
             val response = new BasicClassicHttpResponse(200)) {
            response.setEntity(new InputStreamEntity(input, declaredLength, ContentType.APPLICATION_OCTET_STREAM));
            assertThrows(IOException.class, () -> HttpRequestUtils.responseHandler(16).handleResponse(response));
            assertEquals(15, input.available());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 16})
    void verifyResponseAtSizeLimit(final long declaredLength) throws Exception {
        val body = "1234567890123456";
        try (val response = new BasicClassicHttpResponse(201, "Created")) {
            response.setHeader("Content-Type", "text/plain; charset=UTF-8");
            response.setHeader("ETag", "metadata-version");
            response.setLocale(Locale.CANADA);
            response.setVersion(HttpVersion.HTTP_1_0);
            response.setEntity(new InputStreamEntity(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
                declaredLength, ContentType.TEXT_PLAIN));
            try (val buffered = HttpRequestUtils.responseHandler(16).handleResponse(response)) {
                assertEquals(body, EntityUtils.toString(buffered.getEntity()));
                assertEquals(201, buffered.getCode());
                assertEquals("Created", buffered.getReasonPhrase());
                assertEquals("metadata-version", buffered.getFirstHeader("ETag").getValue());
                assertEquals(Locale.CANADA, buffered.getLocale());
                assertEquals(HttpVersion.HTTP_1_0, buffered.getVersion());
                assertEquals("text/plain; charset=UTF-8", buffered.getEntity().getContentType());
            }
        }
    }

    @Test
    void verifyResponseWithoutEntity() throws Exception {
        try (val response = new BasicClassicHttpResponse(204);
             val buffered = HttpRequestUtils.responseHandler(16).handleResponse(response)) {
            assertEquals(204, buffered.getCode());
            assertNull(buffered.getEntity());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void verifyResponseDefaultSizeLimit(final long maximumSize) throws Exception {
        val entity = mock(HttpEntity.class);
        when(entity.getContentLength()).thenReturn(Long.MAX_VALUE);
        try (val response = new BasicClassicHttpResponse(200)) {
            response.setEntity(entity);
            assertThrows(IOException.class, () -> HttpRequestUtils.responseHandler(maximumSize).handleResponse(response));
            assertThrows(IOException.class, () -> HttpRequestUtils.HTTP_CLIENT_RESPONSE_HANDLER.handleResponse(response));
            verify(entity, never()).getContent();
        }
    }

    @Test
    void verifyBadGeoLocation() {
        val request = new MockHttpServletRequest();
        request.setParameter("geolocation", "34,45,1,12345");
        assertNotNull(HttpRequestUtils.getHttpServletRequestGeoLocation(request));

        request.setParameter("geolocation", "34,4");
        assertNotNull(HttpRequestUtils.getHttpServletRequestGeoLocation(request));

        request.setParameter("geolocation", "34,4,,1");
        assertNotNull(HttpRequestUtils.getHttpServletRequestGeoLocation(request));
    }

    @Test
    void verifyNoRequest() {
        assertNull(HttpRequestUtils.getHttpServletRequestFromRequestAttributes());
    }

    @Test
    void verifyNoLoc() {
        val loc = HttpRequestUtils.getHttpServletRequestGeoLocation(new MockHttpServletRequest());
        assertNull(loc.getLongitude());
    }

    @Test
    void verifyHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader("h1", "v1");
        request.addHeader("h2", "v2");
        assertNotNull(HttpRequestUtils.getRequestHeaders(request));
    }

    @Test
    void verifyPing() {
        assertNotNull(HttpRequestUtils.pingUrl("https://github.com"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, HttpRequestUtils.pingUrl("bad-endpoint"));
    }
}
