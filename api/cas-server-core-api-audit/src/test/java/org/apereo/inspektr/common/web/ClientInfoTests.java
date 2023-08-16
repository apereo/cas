package org.apereo.inspektr.common.web;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientInfoTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
public class ClientInfoTests {
    @Test
    void verifyClientInfoCreation() throws Throwable {
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
    void verifyClientInfoFromHttp() throws Throwable {
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
    void verifyClientInfoWithServerHost() throws Throwable {
        try {
            val request = getHttpServletRequest();
            ClientInfoHolder.setClientInfo(ClientInfo.from(request, "server-header", "client-header", true));
            val clientInfo = ClientInfo.from(request);
            ClientInfoHolder.setClientInfo(clientInfo);
            val foundInfo = ClientInfoHolder.getClientInfo();
            assertNotEquals(foundInfo, ClientInfo.empty());
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Test
    void verifyClientInfoWithoutServerHost() throws Throwable {
        try {
            val request = getHttpServletRequest();
            ClientInfoHolder.setClientInfo(ClientInfo.from(request, "server-header", "client-header", false));
            val clientInfo = ClientInfo.from(request);
            ClientInfoHolder.setClientInfo(clientInfo);
            val foundInfo = ClientInfoHolder.getClientInfo();
            assertNotEquals(foundInfo, ClientInfo.empty());
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Nonnull
    private static MockHttpServletRequest getHttpServletRequest() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader("user-agent", "firefox");
        request.addHeader("server-header", "1.2.3.4");
        request.addHeader("client-header", "5.6.7.8");
        return request;
    }
}
