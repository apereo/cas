package org.apereo.cas.support.geo.maxmind.config;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasGeoLocationAutoConfiguration;
import org.apereo.cas.config.CasGeoLocationMaxmindAutoConfiguration;
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
 * This is {@link CasGeoLocationMaxmindConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasGeoLocationAutoConfiguration.class,
    CasGeoLocationMaxmindAutoConfiguration.class
})
@Tag("GeoLocation")
@ExtendWith(CasTestExtension.class)
class CasGeoLocationMaxmindConfigurationTests {
    @Autowired
    @Qualifier(GeoLocationService.BEAN_NAME)
    private GeoLocationService geoLocationService;

    @Test
    void verifyOperation() {
        assertNotNull(geoLocationService);
    }
}
