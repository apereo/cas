package org.apereo.cas.api;

/**
 * This is {@link AuthenticationRiskScore}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskScore {
    private double score;

    public AuthenticationRiskScore(final double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
}
