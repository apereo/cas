package org.apereo.cas.support.geo.azure;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasGeoLocationAutoConfiguration;
import org.apereo.cas.config.CasGeoLocationAzureMapsAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.InetAddress;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AzureMapsGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(
    classes = {
        CasGeoLocationAutoConfiguration.class,
        CasGeoLocationAzureMapsAutoConfiguration.class
    },
    properties = {
        "cas.geo-location.azure.client-id=${#environmentVars['AZURE_MAPS_CLIENT_ID']}",
        "cas.geo-location.azure.api-key=${#environmentVars['AZURE_MAPS_API_KEY']}"
    })
@Tag("GeoLocation")
@EnabledIfEnvironmentVariable(named = "AZURE_MAPS_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AZURE_MAPS_CLIENT_ID", matches = ".+")
class AzureMapsGeoLocationServiceTests {
    @Autowired
    @Qualifier(GeoLocationService.BEAN_NAME)
    private GeoLocationService geoLocationService;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(geoLocationService.locate("8.8.8.8"));
        val resp = geoLocationService.locate(40.689060, -74.044636);
        assertEquals(40, Double.valueOf(resp.getLatitude()).intValue());
        assertEquals(-74, Double.valueOf(resp.getLongitude()).intValue());
        assertFalse(resp.getAddresses().isEmpty());
        assertDoesNotThrow(() -> {
            geoLocationService.locate(InetAddress.getByName("www.github.com"));
        });
    }
}
