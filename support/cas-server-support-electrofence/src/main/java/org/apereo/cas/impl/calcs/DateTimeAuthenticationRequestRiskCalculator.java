package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DateTimeAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    private static final int ERROR_MARGIN = 2;
    
    public DateTimeAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        super(casEventRepository);
    }

    @Override
    protected double calculateScore(final HttpServletRequest request, final Authentication authentication, 
                                    final RegisteredService service, final Collection<CasEvent> events) {
        final ZonedDateTime timestamp = ZonedDateTime.now();
        logger.debug("Filtering authentication events for timestamp {}", timestamp);
        
        final long count = events.stream().filter(e -> e.getCreationTime().getHour() == timestamp.getHour()
                || e.getCreationTime().plusHours(ERROR_MARGIN).getHour() == timestamp.getHour()
                || e.getCreationTime().minusHours(ERROR_MARGIN).getHour() == timestamp.getHour()).count();
        
        logger.debug("Total authentication events found for {}: {}", timestamp, count);
        if (count == events.size()) {
            logger.debug("Principal {} has always authenticated from {}", authentication.getPrincipal(), timestamp);
            return LOWEST_RISK_SCORE;
        }
        return HIGHEST_RISK_SCORE - (count / events.size());
    }
}
