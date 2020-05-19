package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.trusted.web.flow.fingerprint.GeoLocationDeviceFingerprintComponentExtractor;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GeoLocationDeviceFingerprintComponentExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class GeoLocationDeviceFingerprintComponentExtractorTests {
    @Test
    public void verifyGeoLocationDevice() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "TestAgent");
        request.setParameter("geolocation", "40,70,1000,100");

        val geoResp = new GeoLocationResponse();
        geoResp.addAddress("GeoAddress");
        val geoLocationService = mock(GeoLocationService.class);
        when(geoLocationService.locate(anyDouble(), anyDouble())).thenReturn(geoResp);
        when(geoLocationService.locate(any(GeoLocationRequest.class))).thenReturn(geoResp);
        val ex = new GeoLocationDeviceFingerprintComponentExtractor(geoLocationService);
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val result = ex.extractComponent("casuser", context, true);
        assertTrue(result.isPresent());
    }

}
