package org.apereo.cas.trusted.web.flow.fingerprint;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Provides the geo location for device fingerprint generation.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class GeoLocationDeviceFingerprintComponentManager implements DeviceFingerprintComponentManager {
    private final GeoLocationService geoLocationService;

    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extractComponent(final String principal,
                                             final HttpServletRequest request,
                                             final HttpServletResponse response) {
        val loc = WebUtils.getHttpServletRequestGeoLocation(request);

        if (loc != null && loc.isValid()) {
            LOGGER.trace("Attempting to geolocate [{}]", loc);
            val geoResponse = geoLocationService.locate(loc);
            val address = geoResponse.build();
            if (StringUtils.isBlank(address)) {
                return getDefaultGeoLocation(loc);
            }
            return Optional.of(address);
        }
        LOGGER.trace("No geolocation could be determined from the request");
        return Optional.empty();
    }

    private static Optional<String> getDefaultGeoLocation(final GeoLocationRequest loc) {
        return Optional.of(loc.getLatitude() + '-' + loc.getLongitude());
    }
}
