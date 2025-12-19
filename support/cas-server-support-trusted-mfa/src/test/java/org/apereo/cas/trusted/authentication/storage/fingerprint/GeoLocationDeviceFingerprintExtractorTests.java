package org.apereo.cas.trusted.authentication.storage.fingerprint;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.web.flow.fingerprint.GeoLocationDeviceFingerprintExtractor;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GeoLocationDeviceFingerprintExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("GeoLocation")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class GeoLocationDeviceFingerprintExtractorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyGeoLocationDevice() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("1.2.3.4");
        context.withUserAgent();
        context.setParameter("geolocation", "40,70,1000,100");

        val geoResp = new GeoLocationResponse();
        geoResp.addAddress("GeoAddress");
        val geoLocationService = mock(GeoLocationService.class);
        when(geoLocationService.locate(anyDouble(), anyDouble())).thenReturn(geoResp);
        when(geoLocationService.locate(any(GeoLocationRequest.class))).thenReturn(geoResp);
        val ex = new GeoLocationDeviceFingerprintExtractor(geoLocationService);
        val result = ex.extract(RegisteredServiceTestUtils.getAuthentication(),
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
        val ex = new GeoLocationDeviceFingerprintExtractor(geoLocationService);
        val result = ex.extract(RegisteredServiceTestUtils.getAuthentication(),
            context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(result.isPresent());
    }

}
