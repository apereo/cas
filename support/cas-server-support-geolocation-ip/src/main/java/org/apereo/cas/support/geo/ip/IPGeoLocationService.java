package org.apereo.cas.support.geo.ip;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.support.geo.AbstractGeoLocationService;

import io.ipgeolocation.api.Geolocation;
import io.ipgeolocation.api.GeolocationParams;
import io.ipgeolocation.api.IPGeolocationAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;

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
        val geoParams = new GeolocationParams();
        geoParams.setIPAddress(address.getHostAddress());
        geoParams.setFields("geo");
        LOGGER.debug("Fetching geolocation results for [{}]", geoParams.getIPAddress());
        val geolocation = api.getGeolocation(geoParams);
        LOGGER.debug("Geolocation results for [{}] are [{}]", geoParams.getIPAddress(), geolocation);
        val status = HttpStatus.resolve((Integer) geolocation.getOrDefault("status", HttpStatus.BAD_REQUEST.value()));
        if (status != null && status.is2xxSuccessful()) {
            val location = new GeoLocationResponse();
            val response = (Geolocation) geolocation.get("response");
            location.addAddress(response.getCity());
            location.addAddress(response.getStateProvince());
            location.addAddress(response.getCountryName());
            return location;
        }
        LOGGER.warn("Unable to determine geolocation results for [{}]", geoParams.getIPAddress());
        return null;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        return null;
    }
}
