package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for password.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "password.policy", ignoreUnknownFields = false)
public class PasswordPolicyProperties {

    private static final int DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS = 30;

    private boolean warnAll;

    private int warningDays = DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS;

    private String url = "https://password.example.edu/change";

    public boolean getWarnAll() {
        return warnAll;
    }

    public void setWarnAll(final boolean warnAll) {
        this.warnAll = warnAll;
    }

    public int getWarningDays() {
        return warningDays;
    }

    public void setWarningDays(final int warningDays) {
        this.warningDays = warningDays;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
