package org.apereo.cas.configuration.model.support.saml;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link SamlResponseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.saml.response", ignoreUnknownFields = false)
public class SamlResponseProperties {
    private int skewAllowance;

    public int getSkewAllowance() {
        return skewAllowance;
    }

    public void setSkewAllowance(final int skewAllowance) {
        this.skewAllowance = skewAllowance;
    }
}
