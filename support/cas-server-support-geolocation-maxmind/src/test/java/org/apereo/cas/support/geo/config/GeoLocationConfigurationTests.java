package org.apereo.cas.support.geo.config;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.Test;

/**
 * This is {@link GeoLocationConfigurationTests}.
 *
 * @author Dmitriy Kopylenko
 * @since 6.0.0
 */
public class GeoLocationConfigurationTests {

    @Test(expected = IllegalArgumentException.class)
    public void verifyNoNpeIsThrownIfNoDatabaseConfigPropertiesArePresent() {
        val geoLocationConfigUnderTest = new CasGeoLocationConfiguration(new CasConfigurationProperties());
        geoLocationConfigUnderTest.geoLocationService();
    }
}
