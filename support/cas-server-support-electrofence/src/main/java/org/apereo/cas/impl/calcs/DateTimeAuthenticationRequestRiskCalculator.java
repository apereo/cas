package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collection;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DateTimeAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {

    private final int windowInHours;

    public DateTimeAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository, final int windowInHours) {
        super(casEventRepository);
        this.windowInHours = windowInHours;
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request, final Authentication authentication,
                                        final RegisteredService service, final Collection<? extends CasEvent> events) {
        val timestamp = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Filtering authentication events for timestamp [{}]", timestamp);

        val hoursFromNow = timestamp.plusHours(windowInHours).getHour();
        val hoursBeforeNow = timestamp.minusHours(windowInHours).getHour();

        val count = events
            .stream()
            .map(time -> {
                val instant = ChronoZonedDateTime.from(time.getCreationZonedDateTime()).toInstant();
                val zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
                return zdt.getHour();
            })
            .filter(hour -> hour <= hoursFromNow && hour >= hoursBeforeNow)
            .count();

        LOGGER.debug("Total authentication events found for [{}] in a [{}]h window: [{}]", timestamp, windowInHours, count);
        if (count == events.size()) {
            LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), timestamp);
            return LOWEST_RISK_SCORE;
        }
        return getFinalAveragedScore(count, events.size());
    }
}
