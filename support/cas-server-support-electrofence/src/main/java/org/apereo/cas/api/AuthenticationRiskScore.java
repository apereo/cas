package org.apereo.cas.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.apereo.inspektr.common.web.ClientInfo;
import java.math.BigDecimal;

/**
 * This is {@link AuthenticationRiskScore}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@With
public class AuthenticationRiskScore {
    /**
     * Highest risk score for an authn request.
     */
    private static final AuthenticationRiskScore HIGHEST_RISK_SCORE = new AuthenticationRiskScore(BigDecimal.ONE);

    /**
     * Lowest risk score for an authn request.
     */
    private static final AuthenticationRiskScore LOWEST_RISK_SCORE = new AuthenticationRiskScore(BigDecimal.ZERO);

    private final BigDecimal score;

    private ClientInfo clientInfo;

    @JsonIgnore
    public boolean isHighestRisk() {
        return score.compareTo(HIGHEST_RISK_SCORE.getScore()) == 0;
    }

    @JsonIgnore
    public boolean isLowestRisk() {
        return score.compareTo(LOWEST_RISK_SCORE.getScore()) == 0;
    }

    /**
     * Is risk greater than the given threshold?
     *
     * @param threshold the threshold
     * @return true/false
     */
    public boolean isRiskGreaterThan(final double threshold) {
        return score.compareTo(BigDecimal.valueOf(threshold)) > 0;
    }

    /**
     * Highest risk score authentication risk score.
     *
     * @return the authentication risk score
     */
    public static AuthenticationRiskScore highestRiskScore() {
        return HIGHEST_RISK_SCORE;
    }

    /**
     * Lowest risk score authentication risk score.
     *
     * @return the authentication risk score
     */
    public static AuthenticationRiskScore lowestRiskScore() {
        return LOWEST_RISK_SCORE;
    }
}
