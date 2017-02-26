package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DateTimeAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeAuthenticationRequestRiskCalculator.class);
    
    private final int windowInHours;

    public DateTimeAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository, final int windowInHours) {
        super(casEventRepository);
        this.windowInHours = windowInHours;
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request, final Authentication authentication,
                                        final RegisteredService service, final Collection<CasEvent> events) {
        final ZonedDateTime timestamp = ZonedDateTime.now();
        LOGGER.debug("Filtering authentication events for timestamp [{}]", timestamp);

        final long count = events.stream().filter(e -> e.getCreationTime().getHour() == timestamp.getHour()
                || e.getCreationTime().plusHours(windowInHours).getHour() == timestamp.getHour()
                || e.getCreationTime().minusHours(windowInHours).getHour() == timestamp.getHour()).count();
        
        LOGGER.debug("Total authentication events found for [{}]: [{}]", timestamp, count);
        if (count == events.size()) {
            LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), timestamp);
            return LOWEST_RISK_SCORE;
        }
        return getFinalAveragedScore(count, events.size());
    }
}
