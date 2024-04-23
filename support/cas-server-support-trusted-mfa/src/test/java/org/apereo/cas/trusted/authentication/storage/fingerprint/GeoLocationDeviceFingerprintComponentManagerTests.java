package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.trusted.web.flow.fingerprint.GeoLocationDeviceFingerprintComponentManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GeoLocationDeviceFingerprintComponentManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("GeoLocation")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
class GeoLocationDeviceFingerprintComponentManagerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyGeoLocationDevice() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("1.2.3.4");
        context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "TestAgent");
        context.setParameter("geolocation", "40,70,1000,100");

        val geoResp = new GeoLocationResponse();
        geoResp.addAddress("GeoAddress");
        val geoLocationService = mock(GeoLocationService.class);
        when(geoLocationService.locate(anyDouble(), anyDouble())).thenReturn(geoResp);
        when(geoLocationService.locate(any(GeoLocationRequest.class))).thenReturn(geoResp);
        val ex = new GeoLocationDeviceFingerprintComponentManager(geoLocationService);
        val result = ex.extractComponent("casuser",
            context.getHttpServletRequest(), context.getHttpServletResponse());
        assertTrue(result.isPresent());
    }

    @Test
    void verifyNoGeoLocationDevice() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val geoResp = new GeoLocationResponse();
        val geoLocationService = mock(GeoLocationService.class);
        when(geoLocationService.locate(anyDouble(), anyDouble())).thenReturn(geoResp);
        when(geoLocationService.locate(any(GeoLocationRequest.class))).thenReturn(geoResp);
        val ex = new GeoLocationDeviceFingerprintComponentManager(geoLocationService);
        val result = ex.extractComponent("casuser", context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(result.isPresent());
    }

}
