package org.apereo.cas.support.geo;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link AbstractGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractGeoLocationService implements GeoLocationService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public GeoLocationResponse locate(final String clientIp, final GeoLocationRequest location) {
        logger.debug("Attempting to find geolocation for {}", clientIp);
        GeoLocationResponse loc = locate(clientIp);

        if (loc == null && location != null) {
            logger.debug("Attempting to find geolocation for {}", location);

            if (StringUtils.isNotBlank(location.getLatitude()) && StringUtils.isNotBlank(location.getLongitude())) {
                loc = locate(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
            }
        }
        return loc;
    }
}
