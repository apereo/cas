package org.apereo.cas.impl.engine;

import com.google.common.collect.Lists;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEngine;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationRiskEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultAuthenticationRiskEngine implements AuthenticationRiskEngine {
    private final Set<AuthenticationRequestRiskCalculator> calculators;

    public DefaultAuthenticationRiskEngine(final Set<AuthenticationRequestRiskCalculator> calculators) {
        this.calculators = calculators;
    }

    @Override
    public Set<AuthenticationRequestRiskCalculator> getCalculators() {
        return calculators;
    }

    @Override
    public AuthenticationRiskScore eval(final Authentication authentication, 
                                        final RegisteredService service, 
                                        final HttpServletRequest request) {
        final List<AuthenticationRiskScore> scores = Lists.newArrayList();
        this.calculators.stream().forEach(r -> scores.add(r.calculate(request)));
        final double sum = scores.stream().map(r -> r.getScore()).reduce(0D, (a, b) -> a + b);
        return new AuthenticationRiskScore(sum / this.calculators.size());
    }
}
