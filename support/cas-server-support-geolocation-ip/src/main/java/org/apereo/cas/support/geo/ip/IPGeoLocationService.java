package org.apereo.cas.support.geo.ip;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import io.ipgeolocation.sdk.api.IPGeolocationAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    public GeoLocationResponse locate(final InetAddress address) throws Throwable {
        val geolocation = api.getIPGeolocation()
            .lang("en")
            .ip(address.getHostAddress())
            .execute();
        LOGGER.debug("Geolocation results for [{}] are [{}]", address.getHostAddress(), geolocation);
        if (geolocation != null && geolocation.getLocation() != null) {
            val location = new GeoLocationResponse();
            val geoLocation = geolocation.getLocation();
            return location
                .setLatitude(Double.parseDouble(StringUtils.defaultIfBlank(geoLocation.getLatitude(), "0.0")))
                .setLongitude(Double.parseDouble(StringUtils.defaultIfBlank(geoLocation.getLongitude(), "0.0")))
                .addAddress(geoLocation.getCity())
                .addAddress(geoLocation.getStateProv())
                .addAddress(geoLocation.getCountryName())
                .addAddress(geoLocation.getCountryCode3())
                .addAddress(geoLocation.getZipcode());
        }
        LOGGER.warn("Unable to determine geolocation results for [{}]", address.getHostAddress());
        return null;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        return null;
    }
}
