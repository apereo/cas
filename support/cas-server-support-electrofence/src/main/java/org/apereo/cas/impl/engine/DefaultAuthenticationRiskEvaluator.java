package org.apereo.cas.impl.engine;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Audit(action = AuditableActions.EVALUATE_RISKY_AUTHENTICATION,
        actionResolverName = AuditActionResolvers.ADAPTIVE_RISKY_AUTHENTICATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.ADAPTIVE_RISKY_AUTHENTICATION_RESOURCE_RESOLVER)
    @Override
    public AuthenticationRiskScore eval(final Authentication authentication,
                                        final RegisteredService service,
                                        final HttpServletRequest request) {

        val activeCalculators = this.calculators
            .stream()
            .filter(BeanSupplier::isNotProxy).toList();

        if (activeCalculators.isEmpty()) {
            return new AuthenticationRiskScore(AuthenticationRequestRiskCalculator.HIGHEST_RISK_SCORE);
        }

        val scores = activeCalculators
            .stream()
            .map(r -> r.calculate(authentication, service, request))
            .filter(Objects::nonNull).toList();

        val sum = scores.stream()
            .map(AuthenticationRiskScore::score)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        val score = sum.divide(BigDecimal.valueOf(activeCalculators.size()), 2, RoundingMode.UP);
        return new AuthenticationRiskScore(score);
    }
}
