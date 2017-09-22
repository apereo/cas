package org.apereo.cas.api;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * This is {@link AuthenticationRiskScore}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskScore {
    private final BigDecimal score;

    public AuthenticationRiskScore(final BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getScore() {
        return score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("score", score)
                .toString();
    }

    public boolean isHighestRisk() {
        return getScore().compareTo(AuthenticationRequestRiskCalculator.HIGHEST_RISK_SCORE) == 0;
    }

    public boolean isLowestRisk() {
        return getScore().compareTo(AuthenticationRequestRiskCalculator.LOWEST_RISK_SCORE) == 0;
    }

    /**
     * Is risk greater than the given threshold?
     *
     * @param threshold the threshold
     * @return true/false
     */
    public boolean isRiskGreaterThan(final double threshold) {
        return getScore().compareTo(BigDecimal.valueOf(threshold)) > 0;
    }
}
