package org.apereo.cas.support.geo.google;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.support.geo.AbstractGeoLocationService;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * This is {@link GoogleMapsGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleMapsGeoLocationService extends AbstractGeoLocationService {

    private final GeoApiContext context;

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        return locate(address.getHostAddress());
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        if (latitude == null || longitude == null) {
            LOGGER.debug("latitude/longitude must not be null in order for geolocation to proceed");
            return null;
        }

        val r = new GeoLocationResponse();
        r.setLatitude(latitude);
        r.setLongitude(longitude);

        val latlng = new LatLng(latitude, longitude);
        try {
            val results = GeocodingApi.reverseGeocode(this.context, latlng).await();
            if (results != null && results.length > 0) {
                Arrays.stream(results)
                    .map(result -> result.formattedAddress)
                    .forEach(r::addAddress);

                return r;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return r;
    }
}
