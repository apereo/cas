package org.apereo.cas.support.geo.azure;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasGeoLocationAzureMapsConfiguration;
import org.apereo.cas.config.CasGeoLocationConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.net.InetAddress;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AzureMapsGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(
    classes = {
        RefreshAutoConfiguration.class,
        CasGeoLocationConfiguration.class,
        CasGeoLocationAzureMapsConfiguration.class
    },
    properties = {
        "cas.geo-location.azure.client-id=${#environmentVars['AZURE_MAPS_CLIENT_ID']}",
        "cas.geo-location.azure.subscription-id=${#environmentVars['AZURE_MAPS_SUBSCRIPTION_ID']}"
    })
@Tag("GeoLocation")
@EnabledIfEnvironmentVariable(named = "AZURE_MAPS_SUBSCRIPTION_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AZURE_MAPS_CLIENT_ID", matches = ".+")
public class AzureMapsGeoLocationServiceTests {
    @Autowired
    @Qualifier(GeoLocationService.BEAN_NAME)
    private GeoLocationService geoLocationService;

    @Test
    void verifyOperation() {
        assertNotNull(geoLocationService.locate("8.8.8.8"));
        val resp = geoLocationService.locate(40.689060, -74.044636);
        assertEquals(37.337029, resp.getLatitude());
        assertEquals(-121.889778, resp.getLongitude());
        assertFalse(resp.getAddresses().isEmpty());
        assertDoesNotThrow(() -> {
            geoLocationService.locate(InetAddress.getByName("www.github.com"));
        });
    }
}
