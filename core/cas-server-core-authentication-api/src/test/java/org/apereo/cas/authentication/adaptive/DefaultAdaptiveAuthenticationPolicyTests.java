package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
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
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val props = new AdaptiveAuthenticationProperties();
        props.setRejectIpAddresses("185\\.86.+");
        val service = mock(GeoLocationService.class);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.banned(), props);
        assertFalse(p.apply(new MockRequestContext(), USER_AGENT, new GeoLocationRequest(51.5, -0.118)));
    }

    @Test
    public void verifyActionUserAgentRejected() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val props = new AdaptiveAuthenticationProperties();
        props.setRejectBrowsers("Mozilla/5.0.+");
        val service = mock(GeoLocationService.class);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertFalse(p.apply(new MockRequestContext(), USER_AGENT, new GeoLocationRequest(51.5, -0.118)));
    }

    @Test
    public void verifyActionGeoLocationRejected() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, USER_AGENT);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val geoRequest = new GeoLocationRequest(51.5, -0.118);
        val props = new AdaptiveAuthenticationProperties();
        props.setRejectCountries("UK");
        val service = mock(GeoLocationService.class);
        val response = new GeoLocationResponse();
        response.addAddress("UK");
        response.setLatitude(Double.valueOf(geoRequest.getLatitude()));
        response.setLongitude(Double.valueOf(geoRequest.getLongitude()));
        when(service.locate(anyString(), any())).thenReturn(response);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertFalse(p.apply(new MockRequestContext(), USER_AGENT, geoRequest));
    }

    @Test
    public void verifyActionGeoLocationPass() {
        val request = new MockHttpServletRequest();
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val geoRequest = new GeoLocationRequest(51.5, -0.118);
        val props = new AdaptiveAuthenticationProperties();

        val service = mock(GeoLocationService.class);
        val response = new GeoLocationResponse();
        response.setLatitude(Double.valueOf(geoRequest.getLatitude()));
        response.setLongitude(Double.valueOf(geoRequest.getLongitude()));
        when(service.locate(anyString(), any())).thenReturn(response);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertTrue(p.apply(new MockRequestContext(), USER_AGENT, geoRequest));
    }

    @Test
    public void verifyActionWithNoClientInfo() {
        val props = new AdaptiveAuthenticationProperties();
        val service = mock(GeoLocationService.class);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertTrue(p.apply(new MockRequestContext(), "something", new GeoLocationRequest()));
    }
}
