package org.apereo.cas.impl.engine;

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
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link DefaultAuthenticationRiskEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@RequiredArgsConstructor
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
            return AuthenticationRiskScore.highestRiskScore();
        }

        val scores = calculators
            .stream()
            .map(riskCalculator -> riskCalculator.calculate(authentication, service, clientInfo))
            .filter(Objects::nonNull)
            .toList();

        val sum = scores
            .stream()
            .map(AuthenticationRiskScore::getScore)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        val score = sum.divide(BigDecimal.valueOf(calculators.size()), 2, RoundingMode.UP);
        return new AuthenticationRiskScore(score).withClientInfo(clientInfo);
    }

    @Override
    public boolean isRiskyAuthenticationScore(final AuthenticationRiskScore score,
                                              final Authentication authentication,
                                              final RegisteredService service) {
        val threshold = casProperties.getAuthn().getAdaptive().getRisk().getCore().getThreshold();
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
