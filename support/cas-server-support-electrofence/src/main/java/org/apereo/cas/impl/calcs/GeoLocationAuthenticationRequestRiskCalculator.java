package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.http.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import java.math.BigDecimal;
import java.util.List;

/**
 * This is {@link GeoLocationAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class GeoLocationAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {

    private final GeoLocationService geoLocationService;

    public GeoLocationAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository,
                                                          final CasConfigurationProperties casProperties,
                                                          final GeoLocationService geoLocationService) {
        super(casEventRepository, casProperties);
        this.geoLocationService = geoLocationService;
    }

    @Override
    protected BigDecimal calculateScore(final ClientInfo clientInfo, final Authentication authentication,
                                        final RegisteredService service, final List<? extends CasEvent> events) {
        val loc = HttpRequestUtils.getHttpServletRequestGeoLocation(clientInfo.getGeoLocation());
        if (loc.isValid()) {
            LOGGER.debug("Filtering authentication events for geolocation [{}]", loc);
            val count = events.stream().filter(e -> e.getGeoLocation().equals(loc)).count();
            LOGGER.debug("Total authentication events found for [{}]: [{}]", loc, count);
            return calculateScoreBasedOnEventsCount(authentication, events, count);
        }
        val remoteAddr = ClientInfoHolder.getClientInfo().getClientIpAddress();
        LOGGER.debug("Filtering authentication events for location based on ip [{}]", remoteAddr);
        val response = geoLocationService.locate(remoteAddr);
        if (response != null) {
            val locationRequest = new GeoLocationRequest(response.getLatitude(), response.getLongitude());
            val count = events
                .stream()
                .filter(e -> e.getGeoLocation().equals(locationRequest))
                .count();
            LOGGER.debug("Total authentication events found for location of [{}]: [{}]", remoteAddr, count);
            return calculateScoreBasedOnEventsCount(authentication, events, count);
        }
        LOGGER.debug("Request does not contain enough geolocation data");
        return AuthenticationRiskScore.highestRiskScore().getScore();
    }
}
