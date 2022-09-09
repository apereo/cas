package org.apereo.cas.api;

import java.math.BigDecimal;

/**
 * This is {@link AuthenticationRiskScore}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record AuthenticationRiskScore(BigDecimal score) {

    public boolean isHighestRisk() {
        return score().compareTo(AuthenticationRequestRiskCalculator.HIGHEST_RISK_SCORE) == 0;
    }

    public boolean isLowestRisk() {
        return score().compareTo(AuthenticationRequestRiskCalculator.LOWEST_RISK_SCORE) == 0;
    }

    /**
     * Is risk greater than the given threshold?
     *
     * @param threshold the threshold
     * @return true/false
     */
    public boolean isRiskGreaterThan(final double threshold) {
        return score().compareTo(BigDecimal.valueOf(threshold)) > 0;
    }
}
