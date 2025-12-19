package org.apereo.cas.authentication.adaptive;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultAdaptiveAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultAdaptiveAuthenticationPolicy implements AdaptiveAuthenticationPolicy {

    private final GeoLocationService geoLocationService;

    private final IPAddressIntelligenceService ipAddressIntelligenceService;

    private final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties;

    @Override
    public boolean isAuthenticationRequestAllowed(final RequestContext requestContext, final String userAgent,
                                                  final GeoLocationRequest location) throws Throwable {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo == null || StringUtils.isBlank(userAgent)) {
            LOGGER.warn("No client IP or user-agent was provided. Skipping adaptive authentication policy...");
            return true;
        }
        val clientIp = clientInfo.getClientIpAddress();
        if (isIpAddressRejected(requestContext, clientIp)) {
            LOGGER.warn("Client IP [{}] is rejected for authentication", clientIp);
            return false;
        }

        if (isUserAgentRejected(userAgent)) {
            LOGGER.warn("User agent [{}] is rejected for authentication", userAgent);
            return false;
        }
        LOGGER.debug("User agent [{}] is authorized to proceed", userAgent);
        if (this.geoLocationService != null && location != null && StringUtils.isNotBlank(clientIp)
            && StringUtils.isNotBlank(this.adaptiveAuthenticationProperties.getPolicy().getRejectCountries())) {
            val loc = this.geoLocationService.locate(clientIp, location);
            if (loc != null) {
                LOGGER.debug("Determined geolocation for [{}] to be [{}]", clientIp, loc);
                if (isGeoLocationCountryRejected(loc)) {
                    LOGGER.warn("Client [{}] is rejected for authentication based on country location", clientIp);
                    return false;
                }
            } else {
                LOGGER.info("Could not determine geolocation for [{}]", clientIp);
            }
        }
        LOGGER.debug("Adaptive authentication policy has authorized client [{}] to proceed.", clientIp);
        return true;
    }

    private boolean isGeoLocationCountryRejected(final GeoLocationResponse finalLoc) {
        val rejectCountries = this.adaptiveAuthenticationProperties.getPolicy().getRejectCountries();
        return StringUtils.isNotBlank(rejectCountries)
               && RegexUtils.find(rejectCountries, finalLoc.build());
    }

    private boolean isUserAgentRejected(final String userAgent) {
        val rejectBrowsers = this.adaptiveAuthenticationProperties.getPolicy().getRejectBrowsers();
        return StringUtils.isNotBlank(rejectBrowsers)
               && RegexUtils.find(rejectBrowsers, userAgent);
    }

    private boolean isIpAddressRejected(final RequestContext requestContext, final String clientIp) throws Throwable {
        LOGGER.trace("Located client IP address as [{}]", clientIp);
        val ipResult = ipAddressIntelligenceService.examine(requestContext, clientIp);
        if (ipResult == null || ipResult.isBanned()) {
            LOGGER.warn("Client IP [{}] is banned", clientIp);
            return true;
        }
        if (ipResult.isRanked()) {
            val threshold = adaptiveAuthenticationProperties.getRisk().getCore().getThreshold();
            if (ipResult.getScore() >= threshold) {
                LOGGER.warn("Client IP [{}] is rejected for authentication because intelligence score [{}] is higher than the configured risk threshold [{}]",
                    clientIp, ipResult.getScore(), threshold);
                return true;
            }
        }
        return false;
    }
}
