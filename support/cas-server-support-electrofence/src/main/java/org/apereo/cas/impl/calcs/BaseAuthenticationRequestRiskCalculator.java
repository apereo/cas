package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link BaseAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseAuthenticationRequestRiskCalculator implements AuthenticationRequestRiskCalculator {


    /**
     * CAS event repository instance.
     */
    protected final CasEventRepository casEventRepository;

    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public final AuthenticationRiskScore calculate(final Authentication authentication,
                                                   final RegisteredService service,
                                                   final HttpServletRequest request) {
        val principal = authentication.getPrincipal();
        val events = getCasTicketGrantingTicketCreatedEventsFor(principal.getId());
        if (events.isEmpty()) {
            return new AuthenticationRiskScore(HIGHEST_RISK_SCORE);
        }
        val score = new AuthenticationRiskScore(calculateScore(request, authentication, service, events));
        LOGGER.debug("Calculated authentication risk score by [{}] is [{}]", getClass().getSimpleName(), score);
        return score;
    }

    /**
     * Calculate score authentication risk score.
     *
     * @param request        the request
     * @param authentication the authentication
     * @param service        the service
     * @param events         the events
     * @return the authentication risk score
     */
    protected BigDecimal calculateScore(final HttpServletRequest request,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final Collection<? extends CasEvent> events) {
        return HIGHEST_RISK_SCORE;
    }

    /**
     * Gets cas ticket granting ticket created events.
     *
     * @param principal the principal
     * @return the cas ticket granting ticket created events for
     */
    protected Collection<? extends CasEvent> getCasTicketGrantingTicketCreatedEventsFor(final String principal) {
        val type = CasTicketGrantingTicketCreatedEvent.class.getName();
        LOGGER.debug("Retrieving events of type [{}] for [{}]", type, principal);

        val date = ZonedDateTime.now(ZoneOffset.UTC)
            .minusDays(casProperties.getAuthn().getAdaptive().getRisk().getDaysInRecentHistory());
        return casEventRepository.getEventsOfTypeForPrincipal(type, principal, date);
    }

    /**
     * Calculate score based on events count big decimal.
     *
     * @param authentication the authentication
     * @param events         the events
     * @param count          the count
     * @return the big decimal
     */
    protected BigDecimal calculateScoreBasedOnEventsCount(final Authentication authentication,
                                                          final Collection<? extends CasEvent> events,
                                                          final long count) {
        if (count == events.size()) {
            LOGGER.debug("Principal [{}] is assigned to the lowest risk score with attempted count of [{}]", authentication.getPrincipal(), count);
            return LOWEST_RISK_SCORE;
        }
        return getFinalAveragedScore(count, events.size());
    }

    /**
     * Gets final averaged score.
     *
     * @param eventCount the event count
     * @param total      the total
     * @return the final averaged score
     */
    protected BigDecimal getFinalAveragedScore(final long eventCount, final long total) {
        val score = BigDecimal.valueOf(eventCount)
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return HIGHEST_RISK_SCORE.subtract(score);
    }
}
