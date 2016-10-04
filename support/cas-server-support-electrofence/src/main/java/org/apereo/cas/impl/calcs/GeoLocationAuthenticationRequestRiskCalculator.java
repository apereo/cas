package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This is {@link GeoLocationAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GeoLocationAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    /**
     * Geolocation service.
     */
    @Autowired
    @Qualifier("geoLocationService")
    protected GeoLocationService geoLocationService;
}
