package org.apereo.cas.web.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;

import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * @author Daniel Frett
 * @since 5.3.0
 */
public class DefaultCasCookieValueManagerTests {
    private static final String CLIENT_IP = "127.0.0.1";
    private static final String USER_AGENT = "Test-Client/1.0.0";
    private static final String VALUE = "cookieValue";

    private DefaultCasCookieValueManager cookieValueManager;

    @Mock
    private HttpServletRequest request;
    @Mock
    private ClientInfo clientInfo;
    @Mock
    private Cookie cookie;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ClientInfoHolder.setClientInfo(clientInfo);
        cookieValueManager = new DefaultCasCookieValueManager(CipherExecutor.noOp(), new CookieProperties());
    }

    @After
    public void cleanup() {
        ClientInfoHolder.clear();
    }

    @Test
    public void verifyEncodeAndDecodeCookie() {
        whenGettingClientIp().thenReturn(CLIENT_IP);
        whenGettingUserAgent().thenReturn(USER_AGENT);

        // test encoding first
        final String encoded = cookieValueManager.buildCookieValue(VALUE, request);
        assertEquals(VALUE + "@" + CLIENT_IP + "@" + USER_AGENT, encoded);

        // now test decoding the cookie
        when(cookie.getValue()).thenReturn(encoded);
        final String decoded = cookieValueManager.obtainCookieValue(cookie, request);
        assertEquals(VALUE, decoded);
    }

    private OngoingStubbing<String> whenGettingClientIp() {
        return when(clientInfo.getClientIpAddress());
    }

    private OngoingStubbing<String> whenGettingUserAgent() {
        return when(request.getHeader(matches(Pattern.compile("User-Agent", Pattern.CASE_INSENSITIVE))));
    }
}
