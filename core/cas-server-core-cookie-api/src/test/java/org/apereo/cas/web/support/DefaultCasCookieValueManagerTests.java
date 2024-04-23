package org.apereo.cas.web.support;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel Frett
 * @since 5.3.0
 */
@Tag("Cookie")
class DefaultCasCookieValueManagerTests {
    private static final String CLIENT_IP = "127.0.0.1";

    private static final String USER_AGENT = "Test-Client/1.0.0";

    private static final String VALUE = "cookieValue";

    private CookieValueManager cookieValueManager;

    private Cookie cookie;

    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    public void initialize() {
        cookie = mock(Cookie.class);

        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr(CLIENT_IP);
        httpServletRequest.setLocalAddr(CLIENT_IP);
        httpServletRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(ClientInfo.from(httpServletRequest));
        cookieValueManager = getCookieValueManager(new TicketGrantingCookieProperties());
    }

    @AfterEach
    public void cleanup() {
        ClientInfoHolder.clear();
    }

    @Test
    void verifySessionPinning() throws Throwable {
        httpServletRequest.removeHeader(HttpRequestUtils.USER_AGENT_HEADER);
        val props = new TicketGrantingCookieProperties().setPinToSession(true);
        assertThrows(IllegalStateException.class,
            () -> getCookieValueManager(props).buildCookieValue(VALUE, httpServletRequest));
        props.setPinToSession(false);
        assertNotNull(getCookieValueManager(props).buildCookieValue(VALUE, httpServletRequest));
    }

    @Test
    void verifySessionPinningAuthorizedOnFailure() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        props.setAllowedIpAddressesPattern("^19.*.3.1\\d\\d");
        val mgr = getCookieValueManager(props);
        var value = mgr.buildCookieValue(VALUE, httpServletRequest);
        assertNotNull(value);
        httpServletRequest.setRemoteAddr("198.127.3.155");
        ClientInfoHolder.setClientInfo(ClientInfo.from(httpServletRequest));
        value = mgr.obtainCookieValue(value, httpServletRequest);
        assertNotNull(value);
    }

    @Test
    void verifyEncodeAndDecodeCookie() throws Throwable {
        val encoded = cookieValueManager.buildCookieValue(VALUE, httpServletRequest);
        assertEquals(String.join("@", VALUE, CLIENT_IP, USER_AGENT), encoded);

        when(cookie.getValue()).thenReturn(encoded);
        val decoded = cookieValueManager.obtainCookieValue(cookie, httpServletRequest);
        assertEquals(VALUE, decoded);
    }

    @Test
    void verifyNoPinning() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        props.setPinToSession(false);
        val mgr = getCookieValueManager(props);
        assertEquals(VALUE, mgr.obtainCookieValue(VALUE, new MockHttpServletRequest()));
    }

    @Test
    void verifyBadValue() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        val mgr = getCookieValueManager(props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue(VALUE, new MockHttpServletRequest()));
    }

    @Test
    void verifyBadCookie() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        val mgr = getCookieValueManager(props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue(VALUE + "@1@", new MockHttpServletRequest()));
    }

    @Test
    void verifyBadIp() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        val mgr = getCookieValueManager(props);
        assertThrows(InvalidCookieException.class, () -> mgr.obtainCookieValue(VALUE + "@1@agent", new MockHttpServletRequest()));
    }

    @Test
    void verifyBadAgent() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        val mgr = getCookieValueManager(props);
        assertThrows(InvalidCookieException.class,
            () -> mgr.obtainCookieValue(VALUE + '@' + ClientInfoHolder.getClientInfo().getClientIpAddress() + "@agent", new MockHttpServletRequest()));
    }

    @Test
    void verifyMissingClientInfo() throws Throwable {
        val props = new TicketGrantingCookieProperties();
        val mgr = getCookieValueManager(props);
        ClientInfoHolder.clear();
        assertThrows(InvalidCookieException.class,
            () -> mgr.obtainCookieValue(VALUE + '@' + CLIENT_IP + '@' + USER_AGENT, new MockHttpServletRequest()));
    }

    @Test
    void verifySessionGeoLocated() throws Throwable {
        val props = new TicketGrantingCookieProperties()
            .setPinToSession(true)
            .setGeoLocateClientSession(true);
        val geo = mock(GeoLocationService.class);
        when(geo.locate(anyString())).thenReturn(new GeoLocationResponse()
            .addAddress("London, UK")
            .setLatitude(156)
            .setLongitude(34));
        val mgr = getCookieValueManager(geo, props);
        val encoded = mgr.buildCookieValue(VALUE, httpServletRequest);
        when(cookie.getValue()).thenReturn(encoded);
        val decoded = mgr.obtainCookieValue(cookie, httpServletRequest);
        assertEquals(VALUE, decoded);
    }

    private static CookieValueManager getCookieValueManager(final PinnableCookieProperties props) {
        return getCookieValueManager(mock(GeoLocationService.class), props);
    }

    private static CookieValueManager getCookieValueManager(
        final GeoLocationService geoLocationService,
        final PinnableCookieProperties props) {
        return new DefaultCasCookieValueManager(CipherExecutor.noOp(),
            new DirectObjectProvider<>(geoLocationService),
            DefaultCookieSameSitePolicy.INSTANCE,
            props);
    }

}
