package org.apereo.cas.support.geo;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;

import io.userinfo.client.UserInfo;
import lombok.Getter;
import lombok.Setter;
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
@Setter
@Getter
public abstract class AbstractGeoLocationService implements GeoLocationService {
    @Override
    public GeoLocationResponse locate(final String address) {
        val info = UserInfo.getInfo(address);
        if (info != null) {
            val pos = info.getPosition();
            if (pos != null && pos.getLatitude() != null && pos.getLongitude() != null) {
                return locate(pos.getLatitude(), pos.getLongitude());
            }
        }
        return null;
    }

    @Override
    public GeoLocationResponse locate(final String clientIp, final GeoLocationRequest location) {
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
    public GeoLocationResponse locate(final GeoLocationRequest request) {
        return locate(Double.valueOf(request.getLatitude()), Double.valueOf(request.getLongitude()));
    }
}
