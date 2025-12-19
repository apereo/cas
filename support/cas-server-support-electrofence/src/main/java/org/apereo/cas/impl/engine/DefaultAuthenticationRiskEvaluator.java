package org.apereo.cas.impl.engine;

import module java.base;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationVerifiedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is {@link DefaultAuthenticationRiskEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, value = CasEventRepository.TRANSACTION_MANAGER_EVENTS)
public class DefaultAuthenticationRiskEvaluator implements AuthenticationRiskEvaluator {
    private final List<AuthenticationRequestRiskCalculator> calculators;
    private final CasConfigurationProperties casProperties;
    private final CasEventRepository casEventRepository;

    @Audit(action = AuditableActions.EVALUATE_RISKY_AUTHENTICATION,
        actionResolverName = AuditActionResolvers.ADAPTIVE_RISKY_AUTHENTICATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.ADAPTIVE_RISKY_AUTHENTICATION_RESOURCE_RESOLVER)
    @Override
    public AuthenticationRiskScore evaluate(final Authentication authentication,
                                            final RegisteredService service,
                                            final ClientInfo clientInfo) {

        if (calculators.isEmpty()) {
            LOGGER.warn("No risk calculators are available to evaluate authentication risk. "
                + "CAS will proceed to regard the authentication attempt as highly risky. Examine your configuration "
                + "and ensure at least one risk calculator is available and enabled to correctly assess authentication risk.");
            return AuthenticationRiskScore.highestRiskScore();
        }

        val scores = calculators
            .stream()
            .map(riskCalculator -> riskCalculator.calculate(authentication, service, clientInfo))
            .filter(Objects::nonNull)
            .toList();

        LOGGER.debug("Collected [{}] risk scores from [{}] risk calculators", scores.size(), calculators.size());
        val sum = scores
            .stream()
            .map(AuthenticationRiskScore::getScore)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        val score = sum.divide(BigDecimal.valueOf(calculators.size()), 2, RoundingMode.UP);
        LOGGER.debug("Final authentication risk score is calculated as [{}]", score);
        return new AuthenticationRiskScore(score).withClientInfo(clientInfo);
    }

    @Override
    public boolean isRiskyAuthenticationScore(final AuthenticationRiskScore score,
                                              final Authentication authentication,
                                              final RegisteredService service) {
        val threshold = casProperties.getAuthn().getAdaptive().getRisk().getCore().getThreshold();
        LOGGER.trace("Comparing risk score [{}] against threshold [{}]", score, threshold);
        return score.isRiskGreaterThan(threshold) && !isRiskyAuthenticationAcceptable(authentication, score);
    }

    protected boolean isRiskyAuthenticationAcceptable(final Authentication authentication,
                                                      final AuthenticationRiskScore score) {
        val historyWindow = Beans.newDuration(casProperties.getAuthn().getAdaptive()
            .getRisk().getResponse().getGetRiskVerificationHistory());
        return casEventRepository.getEventsOfTypeForPrincipal(
                CasRiskyAuthenticationVerifiedEvent.class.getName(),
                authentication.getPrincipal().getId(),
                ZonedDateTime.now(Clock.systemUTC()).minus(historyWindow))
            .anyMatch(event ->
                event.getClientIpAddress().equalsIgnoreCase(score.getClientInfo().getClientIpAddress())
                    && event.getAgent().equalsIgnoreCase(score.getClientInfo().getUserAgent()));
    }
}
