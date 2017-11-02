package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * This is {@link GeoLocationAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GeoLocationAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationAuthenticationRequestRiskCalculator.class);
    
    /**
     * Geolocation service.
     */
    @Autowired
    @Qualifier("geoLocationService")
    protected GeoLocationService geoLocationService;

    public GeoLocationAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        super(casEventRepository);
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request, final Authentication authentication,
                                        final RegisteredService service, final Collection<CasEvent> events) {

        final GeoLocationRequest loc = WebUtils.getHttpServletRequestGeoLocationFromRequestContext();
        if (loc.isValid()) {
            LOGGER.debug("Filtering authentication events for geolocation [{}]", loc);
            final long count = events.stream().filter(e -> e.getGeoLocation().equals(loc)).count();
            LOGGER.debug("Total authentication events found for [{}]: [{}]", loc, count);
            if (count == events.size()) {
                LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), loc);
                return LOWEST_RISK_SCORE;
            }
            return getFinalAveragedScore(count, events.size());
        }
        final String remoteAddr = ClientInfoHolder.getClientInfo().getClientIpAddress();
        LOGGER.debug("Filtering authentication events for location based on ip [{}]", remoteAddr);
        final GeoLocationResponse response = this.geoLocationService.locate(remoteAddr);
        if (response != null) {
            final long count = events.stream().filter(e -> e.getGeoLocation().equals(
                    new GeoLocationRequest(response.getLatitude(), response.getLongitude()))).count();
            LOGGER.debug("Total authentication events found for location of [{}]: [{}]", remoteAddr, count);
            if (count == events.size()) {
                LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), loc);
                return LOWEST_RISK_SCORE;
            }
            return getFinalAveragedScore(count, events.size());
        }
        LOGGER.debug("Request does not contain enough geolocation data");
        return HIGHEST_RISK_SCORE;
    }
}
