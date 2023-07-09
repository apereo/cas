package org.apereo.cas.support.geo.ip;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import io.ipgeolocation.api.GeolocationParams;
import io.ipgeolocation.api.IPGeolocationAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.net.InetAddress;

/**
 * This is {@link IPGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class IPGeoLocationService extends AbstractGeoLocationService {
    private final IPGeolocationAPI api;

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        val geoParams = new GeolocationParams(address.getHostAddress(), "en",
            "geo", null, true, true, true, true, true);
        LOGGER.debug("Fetching geolocation results for [{}]", geoParams.getIPAddress());
        val geolocation = api.getGeolocation(geoParams);
        LOGGER.debug("Geolocation results for [{}] are [{}]", geoParams.getIPAddress(), geolocation);
        if (geolocation != null) {
            val location = new GeoLocationResponse();
            return location
                .addAddress(geolocation.getCity())
                .addAddress(geolocation.getStateProvince())
                .addAddress(geolocation.getCountryName());
        }
        LOGGER.warn("Unable to determine geolocation results for [{}]", geoParams.getIPAddress());
        return null;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        return null;
    }
}
