package org.apereo.cas.support.geo.google;

import org.apereo.cas.support.geo.GeoLocation;
import org.apereo.cas.support.geo.GeoLocationService;

import java.net.InetAddress;

/**
 * This is {@link GoogleMapsGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleMapsGeoLocationService implements GeoLocationService {
    private String apiKey;

    public GoogleMapsGeoLocationService(final String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public GeoLocation locate(final InetAddress address) {
        return null;
    }

    @Override
    public GeoLocation locate(final String address) {
        return null;
    }
    
}
