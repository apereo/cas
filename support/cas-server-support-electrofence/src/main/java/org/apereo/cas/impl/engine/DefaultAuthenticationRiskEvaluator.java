package org.apereo.cas.impl.engine;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationRiskEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DefaultAuthenticationRiskEvaluator implements AuthenticationRiskEvaluator {
    private final Set<AuthenticationRequestRiskCalculator> calculators;

    @Override
    public Set<AuthenticationRequestRiskCalculator> getCalculators() {
        return calculators;
    }

    @Audit(action = "EVALUATE_RISKY_AUTHENTICATION",
        actionResolverName = "ADAPTIVE_RISKY_AUTHENTICATION_ACTION_RESOLVER",
        resourceResolverName = "ADAPTIVE_RISKY_AUTHENTICATION_RESOURCE_RESOLVER")
    @Override
    public AuthenticationRiskScore eval(final Authentication authentication,
                                        final RegisteredService service,
                                        final HttpServletRequest request) {
        if (this.calculators.isEmpty()) {
            return new AuthenticationRiskScore(AuthenticationRequestRiskCalculator.HIGHEST_RISK_SCORE);
        }

        val scores = new ArrayList<AuthenticationRiskScore>(this.calculators.size());
        this.calculators.forEach(r -> scores.add(r.calculate(authentication, service, request)));
        val sum = scores.stream().map(AuthenticationRiskScore::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        val score = sum.divide(BigDecimal.valueOf(this.calculators.size()), 2, RoundingMode.UP);
        return new AuthenticationRiskScore(score);
    }
}
