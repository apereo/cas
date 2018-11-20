package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collection;

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
    protected BigDecimal calculateScore(final HttpServletRequest request, final Authentication authentication,
                                        final RegisteredService service, final Collection<? extends CasEvent> events) {
        val loc = WebUtils.getHttpServletRequestGeoLocation(request);
        if (loc != null && loc.isValid()) {
            LOGGER.debug("Filtering authentication events for geolocation [{}]", loc);
            val count = events.stream().filter(e -> e.getGeoLocation().equals(loc)).count();
            LOGGER.debug("Total authentication events found for [{}]: [{}]", loc, count);
            return calculateScoreBasedOnEventsCount(authentication, events, count);
        }
        val remoteAddr = ClientInfoHolder.getClientInfo().getClientIpAddress();
        LOGGER.debug("Filtering authentication events for location based on ip [{}]", remoteAddr);
        val response = this.geoLocationService.locate(remoteAddr);
        if (response != null) {
            val count = events
                .stream()
                .filter(e -> e.getGeoLocation().equals(new GeoLocationRequest(response.getLatitude(), response.getLongitude())))
                .count();
            LOGGER.debug("Total authentication events found for location of [{}]: [{}]", remoteAddr, count);
            return calculateScoreBasedOnEventsCount(authentication, events, count);
        }
        LOGGER.debug("Request does not contain enough geolocation data");
        return HIGHEST_RISK_SCORE;
    }
}
