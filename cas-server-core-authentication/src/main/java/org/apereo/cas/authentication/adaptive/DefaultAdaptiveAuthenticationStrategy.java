package org.apereo.cas.authentication.adaptive;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link DefaultAdaptiveAuthenticationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultAdaptiveAuthenticationStrategy implements AdaptiveAuthenticationStrategy {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private GeoLocationService geoLocationService;
    
    private AdaptiveAuthenticationProperties adaptiveAuthenticationProperties;
    
    public void setGeoLocationService(final GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    public void setAdaptiveAuthenticationProperties(final AdaptiveAuthenticationProperties a) {
        this.adaptiveAuthenticationProperties = a;
    }

    @Override
    public boolean apply(final String userAgent, final GeoLocationRequest location) {
        if (isUserAgentRejected(userAgent)) {
            logger.warn("User agent {} is rejected for authentication", userAgent);
            return false;
        }

        if (this.geoLocationService != null) {
            final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
            final String clientIp = clientInfo.getClientIpAddress();
            final GeoLocationResponse loc = getGeoLocation(clientIp, location);
            
            if (loc != null) {
                logger.debug("Determined geolocation to be {}", loc);
                if (isGeoLocationCountryRejected(loc)) {
                    logger.warn("Client {} is rejected for authentication", clientIp);
                    return false;
                }
            } else {
                logger.info("Could not determine geolocation for {}", clientIp);
            }
        }
        
        return true;
    }

    private GeoLocationResponse getGeoLocation(final String clientIp, final GeoLocationRequest location) {
        logger.debug("Attempting to find geolocation for {}", clientIp);
        GeoLocationResponse loc = this.geoLocationService.locate(clientIp);

        if (loc == null && location != null) {
            logger.debug("Attempting to find geolocation for {}", location);

            if (StringUtils.isNotBlank(location.getLatitude()) && StringUtils.isNotBlank(location.getLongitude())) {
                loc = this.geoLocationService.locate(Double.valueOf(location.getLatitude()),
                        Double.valueOf(location.getLongitude()));
            }
        }
        return loc;
    }

    private boolean isGeoLocationCountryRejected(final GeoLocationResponse finalLoc) {
        return this.adaptiveAuthenticationProperties.getRejectCountries()
                .stream()
                .filter(s -> finalLoc.getCountry().matches(s))
                .findFirst()
                .isPresent();
    }
    
    private boolean isUserAgentRejected(final String userAgent) {
        return this.adaptiveAuthenticationProperties.getRejectBrowsers()
                .stream()
                .filter(s -> userAgent.matches(s))
                .findFirst()
                .isPresent();
    }
}
