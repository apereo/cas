package org.apereo.cas.impl.calcs;

import module java.base;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

/**
 * This is {@link BaseAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAuthenticationRequestRiskCalculator implements AuthenticationRequestRiskCalculator {

    protected final CasEventRepository casEventRepository;

    protected final CasConfigurationProperties casProperties;

    @Override
    public final AuthenticationRiskScore calculate(final Authentication authentication,
                                                   final RegisteredService service,
                                                   final ClientInfo clientInfo) {
        val principal = authentication.getPrincipal();
        val events = getCasTicketGrantingTicketCreatedEventsFor(principal.getId()).collect(Collectors.toList());
        if (events.isEmpty()) {
            return AuthenticationRiskScore.highestRiskScore();
        }
        val score = calculateScore(clientInfo, authentication, service, events);
        val authenticationRiskScore = new AuthenticationRiskScore(score).withClientInfo(ClientInfoHolder.getClientInfo());
        LOGGER.debug("Calculated authentication risk score by [{}] is [{}]", getClass().getSimpleName(), authenticationRiskScore);
        return authenticationRiskScore;
    }

    protected BigDecimal calculateScore(final ClientInfo clientInfo,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final List<? extends CasEvent> events) {
        return AuthenticationRiskScore.highestRiskScore().getScore();
    }

    protected Stream<? extends CasEvent> getCasTicketGrantingTicketCreatedEventsFor(final String principal) {
        val type = CasTicketGrantingTicketCreatedEvent.class.getName();
        LOGGER.debug("Retrieving events of type [{}] for [{}]", type, principal);

        val date = ZonedDateTime.now(ZoneOffset.UTC)
            .minusDays(casProperties.getAuthn().getAdaptive().getRisk().getCore().getDaysInRecentHistory());
        return casEventRepository.getEventsOfTypeForPrincipal(type, principal, date);
    }

    protected BigDecimal calculateScoreBasedOnEventsCount(final Authentication authentication,
                                                          final List<? extends CasEvent> events,
                                                          final long count) {
        val eventCount = events.size();
        if (count == eventCount) {
            LOGGER.debug("Principal [{}] is assigned to the lowest risk score with attempted count of [{}]",
                authentication.getPrincipal(), count);
            return AuthenticationRiskScore.lowestRiskScore().getScore();
        }
        return getFinalAveragedScore(count, eventCount);
    }

    protected BigDecimal getFinalAveragedScore(final long eventCount, final long total) {
        val score = BigDecimal.valueOf(eventCount)
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return AuthenticationRiskScore.highestRiskScore().getScore().subtract(score);
    }
}
