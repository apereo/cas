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
import java.time.ZoneOffset;
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
        final ZonedDateTime timestamp = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Filtering authentication events for timestamp [{}]", timestamp);
        long count = 0;

        if (timestamp.getHour() <= timestamp.plusHours(windowInHours).getHour()
                && timestamp.getHour() >= timestamp.minusHours(windowInHours).getHour()){
            count = events.stream().filter(e->e.getCreationTime().getHour() <= timestamp.plusHours(windowInHours).getHour()
                && e.getCreationTime().getHour() >= timestamp.minusHours(windowInHours).getHour()).count();
        } else {
            count = events.stream().filter(e -> e.getCreationTime().getHour() <= timestamp.plusHours(windowInHours).getHour()
                || e.getCreationTime().getHour() >= timestamp.minusHours(windowInHours).getHour()).count();
        }

        LOGGER.debug("Total authentication events found for [{}] in a [{}]h window: [{}]", timestamp, windowInHours, count);
        if (count == events.size()) {
            LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), timestamp);
            return LOWEST_RISK_SCORE;
        }
        return getFinalAveragedScore(count, events.size());
    }
}
