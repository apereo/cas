package org.apereo.cas.support.geo.ip;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasGeoLocationAutoConfiguration;
import org.apereo.cas.config.CasIPGeoLocationAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link IPGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasGeoLocationAutoConfiguration.class,
    CasIPGeoLocationAutoConfiguration.class
},
    properties = "cas.geo-location.ip-geo-location.api-key=babbec2c1dd94f5899e25f8808e01aed")
@Tag("GeoLocation")
@ExtendWith(CasTestExtension.class)
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
