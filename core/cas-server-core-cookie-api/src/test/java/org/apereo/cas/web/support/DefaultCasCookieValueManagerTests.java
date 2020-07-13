package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel Frett
 * @since 5.3.0
 */
@Tag("Simple")
public class DefaultCasCookieValueManagerTests {
    private static final String CLIENT_IP = "127.0.0.1";

    private static final String USER_AGENT = "Test-Client/1.0.0";

    private static final String VALUE = "cookieValue";

    private DefaultCasCookieValueManager cookieValueManager;
    
    @Mock
    private Cookie cookie;

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);

        val request = new MockHttpServletRequest();
        request.setRemoteAddr(CLIENT_IP);
        request.setLocalAddr(CLIENT_IP);
        request.addHeader("User-Agent", USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        cookieValueManager = new DefaultCasCookieValueManager(CipherExecutor.noOp(), new TicketGrantingCookieProperties());
    }

    @AfterEach
    public void cleanup() {
        ClientInfoHolder.clear();
    }

    @Test
    public void verifyEncodeAndDecodeCookie() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr(CLIENT_IP);
        request.setLocalAddr(CLIENT_IP);
        request.addHeader("User-Agent", USER_AGENT);
        val encoded = cookieValueManager.buildCookieValue(VALUE, request);
        assertEquals(String.join("@", VALUE, CLIENT_IP, USER_AGENT), encoded);

        when(cookie.getValue()).thenReturn(encoded);
        val decoded = cookieValueManager.obtainCookieValue(cookie, request);
        assertEquals(VALUE, decoded);
    }

    @Test
    public void verifyNoPinning() {
        val props = new TicketGrantingCookieProperties();
        props.setPinToSession(false);
        val mgr = new DefaultCasCookieValueManager(CipherExecutor.noOp(), props);
        assertEquals("something", mgr.obtainCookieValue("something", new MockHttpServletRequest()));
    }

    @Test
    public void verifyBadValue() {
        val props = new TicketGrantingCookieProperties();
        val mgr = new DefaultCasCookieValueManager(CipherExecutor.noOp(), props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue("something", new MockHttpServletRequest()));
    }

    @Test
    public void verifyBadCookie() {
        val props = new TicketGrantingCookieProperties();
        val mgr = new DefaultCasCookieValueManager(CipherExecutor.noOp(), props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue("something@1@", new MockHttpServletRequest()));
    }

    @Test
    public void verifyBadIp() {
        val props = new TicketGrantingCookieProperties();
        val mgr = new DefaultCasCookieValueManager(CipherExecutor.noOp(), props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue("something@1@agent", new MockHttpServletRequest()));
    }

    @Test
    public void verifyBadAgent() {
        val props = new TicketGrantingCookieProperties();
        val mgr = new DefaultCasCookieValueManager(CipherExecutor.noOp(), props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue("something@"
            + ClientInfoHolder.getClientInfo().getClientIpAddress() + "@agent", new MockHttpServletRequest()));
    }
}
