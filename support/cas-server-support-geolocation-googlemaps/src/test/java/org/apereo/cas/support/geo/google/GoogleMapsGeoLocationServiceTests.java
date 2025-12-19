package org.apereo.cas.support.geo.google;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasGeoLocationAutoConfiguration;
import org.apereo.cas.config.CasGoogleMapsGeoCodingAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleMapsGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasGeoLocationAutoConfiguration.class,
    CasGoogleMapsGeoCodingAutoConfiguration.class
}, properties = "cas.geo-location.google-maps.api-key=AIzaSyCea6zDOkwJVIOm0vZyAI5eHYrz9Vzlhi9")
@Tag("GeoLocation")
@ExtendWith(CasTestExtension.class)
class GoogleMapsGeoLocationServiceTests {
    @Autowired
    @Qualifier(GeoLocationService.BEAN_NAME)
    private GeoLocationService geoLocationService;

    @Test
    void verifyOperation() throws Throwable {
        assertNull(geoLocationService.locate("8.8.8.8"));
        assertNull(geoLocationService.locate(null, 12.123));
        val resp = geoLocationService.locate(40.689060, -74.044636);
        assertEquals(40.689060, resp.getLatitude());
        assertEquals(-74.044636, resp.getLongitude());
        assertTrue(resp.getAddresses().isEmpty());
        assertDoesNotThrow(() -> {
            geoLocationService.locate(InetAddress.getByName("www.github.com"));
        });
    }

    @Test
    void verifyGeocode() {
        val service = new GoogleMapsGeoLocationService(mock(GeoApiContext.class)) {
            @Override
            protected GeocodingResult[] reverseGeocode(final LatLng latlng) {
                var result = mock(GeocodingResult.class);
                when(result.formattedAddress).thenReturn("address1");
                return new GeocodingResult[]{result};
            }
        };
        val resp = service.locate(40.689060, -74.044636);
        assertNotNull(resp);
    }
}
