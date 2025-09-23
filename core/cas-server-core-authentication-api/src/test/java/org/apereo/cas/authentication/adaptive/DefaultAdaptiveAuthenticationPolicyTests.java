package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceResponse;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAdaptiveAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationPolicy")
class DefaultAdaptiveAuthenticationPolicyTests {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0";

    @Test
    void verifyActionClientIpRejected() throws Throwable {
        val context = MockRequestContext.create();
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.88.151.11").withUserAgent(USER_AGENT).setClientInfo();

        val props = new AdaptiveAuthenticationProperties();
        props.getPolicy().setRejectIpAddresses("185\\.86.+");
        val service = mock(GeoLocationService.class);
        var policy = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.banned(), props);
        val location = new GeoLocationRequest(51.5, -0.118);
        assertFalse(policy.isAuthenticationRequestAllowed(context, USER_AGENT, location));

        policy = new DefaultAdaptiveAuthenticationPolicy(service, (__, clientIpAddress) -> IPAddressIntelligenceResponse.builder()
            .status(IPAddressIntelligenceResponse.IPAddressIntelligenceStatus.RANKED)
            .score(12.15)
            .build(), props);
        assertFalse(policy.isAuthenticationRequestAllowed(new MockRequestContext(), USER_AGENT, location));
    }

    @Test
    void verifyActionUserAgentRejected() throws Throwable {
        val context = MockRequestContext.create();
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.88.151.11").withUserAgent(USER_AGENT).setClientInfo();

        val props = new AdaptiveAuthenticationProperties();
        props.getPolicy().setRejectBrowsers("Mozilla/5.0.+");
        val service = mock(GeoLocationService.class);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertFalse(p.isAuthenticationRequestAllowed(context, USER_AGENT, new GeoLocationRequest(51.5, -0.118)));
    }

    @Test
    void verifyActionGeoLocationRejected() throws Throwable {
        val context = MockRequestContext.create();
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.88.151.11").withUserAgent(USER_AGENT).setClientInfo();

        val geoRequest = new GeoLocationRequest(51.5, -0.118);
        val props = new AdaptiveAuthenticationProperties();
        props.getPolicy().setRejectCountries("UK");
        val service = mock(GeoLocationService.class);
        val response = new GeoLocationResponse();
        response.addAddress("UK");
        response.setLatitude(Double.parseDouble(geoRequest.getLatitude()));
        response.setLongitude(Double.parseDouble(geoRequest.getLongitude()));
        when(service.locate(anyString(), any())).thenReturn(response);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertFalse(p.isAuthenticationRequestAllowed(context, USER_AGENT, geoRequest));
    }

    @Test
    void verifyActionGeoLocationPass() throws Throwable {
        val context = MockRequestContext.create().setClientInfo();

        val geoRequest = new GeoLocationRequest(51.5, -0.118);
        val props = new AdaptiveAuthenticationProperties();

        val service = mock(GeoLocationService.class);
        val response = new GeoLocationResponse();
        response.setLatitude(Double.parseDouble(geoRequest.getLatitude()));
        response.setLongitude(Double.parseDouble(geoRequest.getLongitude()));
        when(service.locate(anyString(), any())).thenReturn(response);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertTrue(p.isAuthenticationRequestAllowed(context, USER_AGENT, geoRequest));
    }

    @Test
    void verifyActionWithNoClientInfo() throws Throwable {
        val props = new AdaptiveAuthenticationProperties();
        val service = mock(GeoLocationService.class);
        val p = new DefaultAdaptiveAuthenticationPolicy(service, IPAddressIntelligenceService.allowed(), props);
        assertTrue(p.isAuthenticationRequestAllowed(new MockRequestContext(), "something", new GeoLocationRequest()));
    }
}
