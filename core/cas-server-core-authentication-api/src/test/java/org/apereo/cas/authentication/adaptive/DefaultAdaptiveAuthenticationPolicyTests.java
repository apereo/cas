package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAdaptiveAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultAdaptiveAuthenticationPolicyTests {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0";

    @Test
    public void verifyActionClientIpRejected() {
        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final var props = new AdaptiveAuthenticationProperties();
        props.setRejectIpAddresses("185\\.86.+");
        final var service = mock(GeoLocationService.class);
        final var p = new DefaultAdaptiveAuthenticationPolicy(service, props);
        assertFalse(p.apply(USER_AGENT, new GeoLocationRequest(51.5, -0.118)));
    }

    @Test
    public void verifyActionUserAgentRejected() {
        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final var props = new AdaptiveAuthenticationProperties();
        props.setRejectBrowsers("Mozilla/5.0.+");
        final var service = mock(GeoLocationService.class);
        final var p = new DefaultAdaptiveAuthenticationPolicy(service, props);
        assertFalse(p.apply(USER_AGENT, new GeoLocationRequest(51.5, -0.118)));
    }

    @Test
    public void verifyActionGeoLocationRejected() {
        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final var geoRequest = new GeoLocationRequest(51.5, -0.118);
        final var props = new AdaptiveAuthenticationProperties();
        props.setRejectCountries("UK");
        final var service = mock(GeoLocationService.class);
        final var response = new GeoLocationResponse();
        response.addAddress("UK");
        response.setLatitude(Double.valueOf(geoRequest.getLatitude()));
        response.setLongitude(Double.valueOf(geoRequest.getLongitude()));
        when(service.locate(anyString(), any())).thenReturn(response);
        final var p = new DefaultAdaptiveAuthenticationPolicy(service, props);
        assertFalse(p.apply(USER_AGENT, geoRequest));
    }

    @Test
    public void verifyActionGeoLocationPass() {
        final var request = new MockHttpServletRequest();
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final var geoRequest = new GeoLocationRequest(51.5, -0.118);
        final var props = new AdaptiveAuthenticationProperties();
       
        final var service = mock(GeoLocationService.class);
        final var response = new GeoLocationResponse();
        response.setLatitude(Double.valueOf(geoRequest.getLatitude()));
        response.setLongitude(Double.valueOf(geoRequest.getLongitude()));
        when(service.locate(anyString(), any())).thenReturn(response);
        final var p = new DefaultAdaptiveAuthenticationPolicy(service, props);
        assertTrue(p.apply(USER_AGENT, geoRequest));
    }

    @Test
    public void verifyActionWithNoClientInfo() {
        final var props = new AdaptiveAuthenticationProperties();
        final var service = mock(GeoLocationService.class);
        final var p = new DefaultAdaptiveAuthenticationPolicy(service, props);
        assertTrue(p.apply("something", new GeoLocationRequest()));
    }
}
