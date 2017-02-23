package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link BaseAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationRequestRiskCalculator implements AuthenticationRequestRiskCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAuthenticationRequestRiskCalculator.class);
    
    /**
     * CAS event repository instance.
     */
    protected CasEventRepository casEventRepository;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    public BaseAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        this.casEventRepository = casEventRepository;
    }

    @Override
    public final AuthenticationRiskScore calculate(final Authentication authentication,
                                                   final RegisteredService service,
                                                   final HttpServletRequest request) {
        final Principal principal = authentication.getPrincipal();
        final Collection<CasEvent> events = getCasTicketGrantingTicketCreatedEventsFor(principal.getId());
        if (events.isEmpty()) {
            return new AuthenticationRiskScore(HIGHEST_RISK_SCORE);
        }
        final AuthenticationRiskScore score = new AuthenticationRiskScore(calculateScore(request, authentication, service, events));
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
                                    final Collection<CasEvent> events) {
        return HIGHEST_RISK_SCORE;
    }

    /**
     * Gets cas ticket granting ticket created events.
     *
     * @param principal the principal
     * @return the cas ticket granting ticket created events for
     */
    protected Collection<CasEvent> getCasTicketGrantingTicketCreatedEventsFor(final String principal) {
        final String type = CasTicketGrantingTicketCreatedEvent.class.getName();
        LOGGER.debug("Retrieving events of type [{}] for [{}]", type, principal);
        
        final ZonedDateTime date = ZonedDateTime.now()
                .minusDays(casProperties.getAuthn().getAdaptive().getRisk().getDaysInRecentHistory());
        return casEventRepository.getEventsOfTypeForPrincipal(type, principal, date);
    }

    /**
     * Gets final averaged score.
     *
     * @param eventCount the event count
     * @param total      the total
     * @return the final averaged score
     */
    protected BigDecimal getFinalAveragedScore(final long eventCount, final long total) {
        final BigDecimal score = BigDecimal.valueOf(eventCount).divide(BigDecimal.valueOf(total), 2, BigDecimal.ROUND_HALF_UP);
        return HIGHEST_RISK_SCORE.subtract(score);
    }
}
