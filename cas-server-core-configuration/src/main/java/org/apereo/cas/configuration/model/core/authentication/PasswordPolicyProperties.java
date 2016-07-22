package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.security.auth.login.LoginException;
import java.util.Map;

/**
 * Configuration properties class for password.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class PasswordPolicyProperties {

    private Map<String, Class<LoginException>> policyAttributes = new LinkedCaseInsensitiveMap<>();
    
    private String warningAttributeValue;
    private String warningAttributeName;
    private boolean displayWarningOnMatch = true;

    private boolean warnAll;

    private int warningDays = 30;

    private String url = "https://password.example.edu/change";

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

    public String getWarningAttributeValue() {
        return warningAttributeValue;
    }

    public void setWarningAttributeValue(final String warningAttributeValue) {
        this.warningAttributeValue = warningAttributeValue;
    }

    public String getWarningAttributeName() {
        return warningAttributeName;
    }

    public void setWarningAttributeName(final String warningAttributeName) {
        this.warningAttributeName = warningAttributeName;
    }

    public boolean isDisplayWarningOnMatch() {
        return displayWarningOnMatch;
    }

    public void setDisplayWarningOnMatch(final boolean displayWarningOnMatch) {
        this.displayWarningOnMatch = displayWarningOnMatch;
    }

    public boolean isWarnAll() {
        return warnAll;
    }

    public Map<String, Class<LoginException>> getPolicyAttributes() {
        return policyAttributes;
    }

    public void setPolicyAttributes(final Map<String, Class<LoginException>> policyAttributes) {
        this.policyAttributes = policyAttributes;
    }
}
