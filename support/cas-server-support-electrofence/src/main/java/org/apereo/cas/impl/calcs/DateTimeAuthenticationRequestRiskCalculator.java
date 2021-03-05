package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.DateTimeUtils;

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

    public DateTimeAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository,
                                                       final CasConfigurationProperties casProperties) {
        super(casEventRepository, casProperties);
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request, final Authentication authentication,
                                        final RegisteredService service, final Collection<? extends CasEvent> events) {
        val windowInHours = casProperties.getAuthn().getAdaptive().getRisk().getDateTime().getWindowInHours();
        val timestamp = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Filtering authentication events for timestamp [{}]", timestamp);

        val hoursFromNow = timestamp.plusHours(windowInHours).getHour();
        val hoursBeforeNow = timestamp.minusHours(windowInHours).getHour();

        val count = events
            .stream()
            .map(time -> {
                val dt = DateTimeUtils.convertToZonedDateTime(time.getCreationTime());
                val instant = ChronoZonedDateTime.from(dt).toInstant();
                val zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
                return zdt.getHour();
            })
            .filter(hour ->
                hoursBeforeNow <= hoursFromNow ? (hour >= hoursBeforeNow && hour <= hoursFromNow) : (hour >= hoursBeforeNow || hour <= hoursFromNow)
            )
            .count();

        LOGGER.debug("Total authentication events found for [{}] in a [{}]h window: [{}]", timestamp, windowInHours, count);
        return calculateScoreBasedOnEventsCount(authentication, events, count);
    }
}
