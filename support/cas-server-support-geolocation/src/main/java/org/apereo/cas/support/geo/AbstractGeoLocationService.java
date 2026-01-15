package org.apereo.cas.support.geo;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link AbstractGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public abstract class AbstractGeoLocationService implements GeoLocationService {
    @Override
    public GeoLocationResponse locate(final String address) {
        return FunctionUtils.doAndHandle(() -> locate(InetAddress.getByName(address)), e -> null).get();
    }

    @Override
    public GeoLocationResponse locate(final String clientIp, final GeoLocationRequest location) throws Throwable {
        LOGGER.trace("Attempting to find geolocation for [{}]", clientIp);
        val loc = locate(clientIp);

        if (loc == null && location != null) {
            LOGGER.trace("Attempting to find geolocation for [{}]", location);
            if (StringUtils.isNotBlank(location.getLatitude()) && StringUtils.isNotBlank(location.getLongitude())) {
                return locate(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
            }
        }
        return loc;
    }

    @Override
    public GeoLocationResponse locate(final GeoLocationRequest request) throws Throwable {
        return locate(Double.valueOf(request.getLatitude()), Double.valueOf(request.getLongitude()));
    }
}
