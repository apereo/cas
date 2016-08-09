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

import java.util.regex.Pattern;

/**
 * This is {@link DefaultAdaptiveAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultAdaptiveAuthenticationPolicy implements AdaptiveAuthenticationPolicy {
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
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String clientIp = clientInfo.getClientIpAddress();
        logger.debug("Located client IP address as {}", clientIp);

        if (isClientIpAddressRejected(clientIp)) {
            logger.warn("Client IP {} is rejected for authentication", clientIp);
            return false;
        }
        
        if (isUserAgentRejected(userAgent)) {
            logger.warn("User agent {} is rejected for authentication", userAgent);
            return false;
        }
        
        logger.debug("User agent {} is authorized to proceed", userAgent);
        
        if (this.geoLocationService != null
            && location != null
            && StringUtils.isNotBlank(clientIp)
            && StringUtils.isNotBlank(this.adaptiveAuthenticationProperties.getRejectCountries())) {
            
            final GeoLocationResponse loc = this.geoLocationService.locate(clientIp, location);
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

        logger.debug("Adaptive authentication policy has authorized client {} to proceed.", clientIp);
        return true;
    }
    


    private boolean isClientIpAddressRejected(final String clientIp) {
        return StringUtils.isNotBlank(this.adaptiveAuthenticationProperties.getRejectIpAddresses())
                && Pattern.compile(this.adaptiveAuthenticationProperties.getRejectIpAddresses()).matcher(clientIp).find();
    }
    
    private boolean isGeoLocationCountryRejected(final GeoLocationResponse finalLoc) {
        return StringUtils.isNotBlank(this.adaptiveAuthenticationProperties.getRejectCountries())
                && Pattern.compile(this.adaptiveAuthenticationProperties.getRejectCountries()).matcher(finalLoc.buildAddress()).find();
    }
    
    private boolean isUserAgentRejected(final String userAgent) {
        return StringUtils.isNotBlank(this.adaptiveAuthenticationProperties.getRejectBrowsers())
                && Pattern.compile(this.adaptiveAuthenticationProperties.getRejectBrowsers()).matcher(userAgent).find();
    }
}
