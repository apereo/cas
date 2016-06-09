package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link PrincipalTransformationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.principal.transform", ignoreUnknownFields = false)
public class PrincipalTransformationProperties {
    private String prefix;
    private String suffix;
    private boolean uppercase;

    public boolean isUppercase() {
        return uppercase;
    }

    public void setUppercase(final boolean uppercase) {
        this.uppercase = uppercase;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }
}
