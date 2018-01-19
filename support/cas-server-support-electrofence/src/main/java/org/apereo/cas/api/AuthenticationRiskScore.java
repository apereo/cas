package org.apereo.cas.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import lombok.ToString;

/**
 * This is {@link AuthenticationRiskScore}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@AllArgsConstructor
@Getter
public class AuthenticationRiskScore {

    private final BigDecimal score;

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
