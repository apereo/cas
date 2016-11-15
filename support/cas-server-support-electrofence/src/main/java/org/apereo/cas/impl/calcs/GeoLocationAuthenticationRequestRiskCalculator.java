package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * This is {@link GeoLocationAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GeoLocationAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
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
    protected double calculateScore(final HttpServletRequest request, final Authentication authentication, 
                                    final RegisteredService service, final Collection<CasEvent> events) {

        final GeoLocationRequest loc = WebUtils.getHttpServletRequestGeoLocation();
        if (loc.isValid()) {
            logger.debug("Filtering authentication events for geolocation {}", loc);
            final long count = events.stream().filter(e -> e.getGeoLocation().equals(loc)).count();
            logger.debug("Total authentication events found for {}: {}", loc, count);
            if (count == events.size()) {
                logger.debug("Principal {} has always authenticated from {}", authentication.getPrincipal(), loc);
                return LOWEST_RISK_SCORE;
            }
            return HIGHEST_RISK_SCORE - (count / events.size());
        }
        logger.debug("Request does not contain enough geolocation data");
        return HIGHEST_RISK_SCORE;
    }
}
