package org.apereo.cas.api;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("score", score)
                .toString();
    }
}
