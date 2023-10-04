package org.apereo.cas.support.geo.ip;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasGeoLocationConfiguration;
import org.apereo.cas.config.IPGeoLocationConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link IPGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasGeoLocationConfiguration.class,
    IPGeoLocationConfiguration.class
},
    properties = "cas.geo-location.ip-geo-location.api-key=babbec2c1dd94f5899e25f8808e01aed")
@Tag("GeoLocation")
class IPGeoLocationServiceTests {
    @Autowired
    @Qualifier(GeoLocationService.BEAN_NAME)
    private GeoLocationService geoLocationService;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(geoLocationService.locate("8.8.8.8"));
        assertNull(geoLocationService.locate("127.0.x.1"));
        assertNull(geoLocationService.locate(34.56, 12.123));
    }
}
