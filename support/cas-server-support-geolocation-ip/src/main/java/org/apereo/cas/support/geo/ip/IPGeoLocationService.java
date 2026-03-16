package org.apereo.cas.support.geo.ip;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import io.ipgeolocation.sdk.IpGeolocationClient;
import io.ipgeolocation.sdk.Language;
import io.ipgeolocation.sdk.LookupIpGeolocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link IPGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class IPGeoLocationService extends AbstractGeoLocationService {
    private final IpGeolocationClient api;

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        val geolocation = api.lookupIpGeolocation(LookupIpGeolocationRequest.builder()
            .ip(address.getHostAddress()).lang(Language.EN).build());

        LOGGER.debug("Geolocation results for [{}] are [{}]", address.getHostAddress(), geolocation);
        if (geolocation.data().location() != null) {
            val location = new GeoLocationResponse();
            val geoLocation = geolocation.data().location();
            return location
                .setLatitude(Double.parseDouble(StringUtils.defaultIfBlank(geoLocation.latitude(), "0.0")))
                .setLongitude(Double.parseDouble(StringUtils.defaultIfBlank(geoLocation.longitude(), "0.0")))
                .addAddress(geoLocation.city())
                .addAddress(geoLocation.stateProv())
                .addAddress(geoLocation.countryNameOfficial())
                .addAddress(geoLocation.countryCode2())
                .addAddress(geoLocation.countryCode3())
                .addAddress(geoLocation.zipcode());
        }
        LOGGER.warn("Unable to determine geolocation results for [{}]", address.getHostAddress());
        return null;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        return null;
    }
}
