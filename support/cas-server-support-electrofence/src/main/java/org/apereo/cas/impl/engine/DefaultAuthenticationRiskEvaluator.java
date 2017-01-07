package org.apereo.cas.impl.engine;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.inspektr.audit.annotation.Audit;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationRiskEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultAuthenticationRiskEvaluator implements AuthenticationRiskEvaluator {
    private final Set<AuthenticationRequestRiskCalculator> calculators;

    public DefaultAuthenticationRiskEvaluator(final Set<AuthenticationRequestRiskCalculator> calculators) {
        this.calculators = calculators;
    }

    @Override
    public Set<AuthenticationRequestRiskCalculator> getCalculators() {
        return calculators;
    }

    @Audit(action = "EVALUATE_RISKY_AUTHENTICATION", actionResolverName = "ADAPTIVE_RISKY_AUTHENTICATION_ACTION_RESOLVER",
            resourceResolverName = "ADAPTIVE_RISKY_AUTHENTICATION_RESOURCE_RESOLVER")
    @Override
    public AuthenticationRiskScore eval(final Authentication authentication,
                                        final RegisteredService service,
                                        final HttpServletRequest request) {
        if (this.calculators.isEmpty()) {
            return new AuthenticationRiskScore(AuthenticationRequestRiskCalculator.HIGHEST_RISK_SCORE);
        }

        final List<AuthenticationRiskScore> scores = new ArrayList<>();
        this.calculators.stream().forEach(r -> scores.add(r.calculate(authentication, service, request)));
        final BigDecimal sum = scores.stream().map(AuthenticationRiskScore::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal score = sum.divide(BigDecimal.valueOf(this.calculators.size()), 2, BigDecimal.ROUND_UP);
        return new AuthenticationRiskScore(score);
    }
}
