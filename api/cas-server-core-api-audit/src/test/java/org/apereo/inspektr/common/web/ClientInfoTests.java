package org.apereo.inspektr.common.web;

import module java.base;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientInfoTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
class ClientInfoTests {
    @Test
    void verifyClientInfoCreation() {
        try {
            val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "Paris")
                .setHeaders(new HashMap<>())
                .setLocale(Locale.ENGLISH)
                .setExtraInfo(new HashMap<>())
                .include("key", "value");
            ClientInfoHolder.setClientInfo(clientInfo);
            val foundInfo = ClientInfoHolder.getClientInfo();
            assertNotNull(foundInfo);
            assertNotNull(foundInfo.getClientIpAddress());
            assertNotNull(foundInfo.getServerIpAddress());
            assertNotNull(foundInfo.getExtraInfo());
            assertNotNull(foundInfo.getGeoLocation());
            assertNotNull(foundInfo.getHeaders());
            assertNotNull(foundInfo.getLocale());
            assertNotNull(foundInfo.getUserAgent());
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Test
    void verifyClientInfoFromHttp() {
        try {
            val request = getHttpServletRequest();
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));
            val clientInfo = ClientInfo.from(request);
            ClientInfoHolder.setClientInfo(clientInfo);
            val foundInfo = ClientInfoHolder.getClientInfo();
            assertNotEquals(foundInfo, ClientInfo.empty());
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Test
    void verifyClientInfoWithServerHost() {
        try {
            val request = getHttpServletRequest();
            val options = ClientInfoExtractionOptions.builder()
                .useServerHostAddress(true)
                .alternateServerAddrHeaderName("server-header")
                .alternateLocalAddrHeaderName("client-header")
                .build();
            ClientInfoHolder.setClientInfo(ClientInfo.from(request, options));
            val clientInfo = ClientInfo.from(request);
            ClientInfoHolder.setClientInfo(clientInfo);
            val foundInfo = ClientInfoHolder.getClientInfo();
            assertNotEquals(foundInfo, ClientInfo.empty());
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Test
    void verifyHeaderExtractionForAllHeaders() {
        try {
            val request = getHttpServletRequest();
            val options = ClientInfoExtractionOptions.builder()
                .useServerHostAddress(true)
                .alternateServerAddrHeaderName("server-header")
                .alternateLocalAddrHeaderName("client-header")
                .httpRequestHeaders(List.of("*"))
                .build();
            val clientInfo = ClientInfo.from(request, options);
            assertTrue(clientInfo.getHeaders().containsKey("server-header"));
            assertTrue(clientInfo.getHeaders().containsKey("client-header"));
            assertTrue(clientInfo.getHeaders().containsKey(HttpHeaders.USER_AGENT));
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Test
    void verifyHeaderExtractionForDefinedHeaders() {
        try {
            val request = getHttpServletRequest();
            val options = ClientInfoExtractionOptions.builder()
                .useServerHostAddress(true)
                .alternateServerAddrHeaderName("server-header")
                .alternateLocalAddrHeaderName("client-header")
                .httpRequestHeaders(List.of(HttpHeaders.USER_AGENT))
                .build();
            val clientInfo = ClientInfo.from(request, options);
            assertFalse(clientInfo.getHeaders().containsKey("server-header"));
            assertFalse(clientInfo.getHeaders().containsKey("client-header"));
            assertTrue(clientInfo.getHeaders().containsKey(HttpHeaders.USER_AGENT));
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Test
    void verifyClientInfoWithoutServerHost() {
        try {
            val request = getHttpServletRequest();
            val options = ClientInfoExtractionOptions.builder()
                .useServerHostAddress(false)
                .alternateServerAddrHeaderName("server-header")
                .alternateLocalAddrHeaderName("client-header")
                .build();
            ClientInfoHolder.setClientInfo(ClientInfo.from(request, options));
            val clientInfo = ClientInfo.from(request);
            ClientInfoHolder.setClientInfo(clientInfo);
            val foundInfo = ClientInfoHolder.getClientInfo();
            assertNotEquals(foundInfo, ClientInfo.empty());
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @NonNull
    private static MockHttpServletRequest getHttpServletRequest() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpHeaders.USER_AGENT, "firefox");
        request.addHeader("server-header", "1.2.3.4");
        request.addHeader("client-header", "5.6.7.8");
        return request;
    }
}
