package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
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

    private boolean enabled = true;
    
    private int loginFailures = 5;
    
    private String warningAttributeValue;
    private String warningAttributeName;
    private boolean displayWarningOnMatch = true;

    private boolean warnAll;
    private int warningDays = 30;
    private AbstractLdapProperties.LdapType type = AbstractLdapProperties.LdapType.GENERIC;

    public AbstractLdapProperties.LdapType getType() {
        return type;
    }

    public void setType(final AbstractLdapProperties.LdapType type) {
        this.type = type;
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

    public int getLoginFailures() {
        return loginFailures;
    }

    public void setLoginFailures(final int loginFailures) {
        this.loginFailures = loginFailures;
    }

    public Map<String, Class<LoginException>> getPolicyAttributes() {
        return policyAttributes;
    }

    public void setPolicyAttributes(final Map<String, Class<LoginException>> policyAttributes) {
        this.policyAttributes = policyAttributes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
