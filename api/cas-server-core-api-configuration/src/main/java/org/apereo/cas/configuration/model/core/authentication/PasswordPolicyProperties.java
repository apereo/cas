package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.Map;

/**
 * Configuration properties class for password.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
public class PasswordPolicyProperties implements Serializable {
    private static final long serialVersionUID = -3878237508646993100L;

    public enum PasswordPolicyHandlingOptions {
        /**
         * Default option to handle policy changes.
         */
        DEFAULT,
        /**
         * Handle account password policies via Groovy.
         */
        GROOVY,
        /**
         * Strategy to only activate password policy
         * if the authentication response code is not blacklisted.
         */
        REJECT_RESULT_CODE
    }

    /**
     * Decide how LDAP authentication should handle password policy changes.
     * Acceptable values are:
     * <ul>
     *     <li>{@code DEFAULT}: Default password policy rules handling account states.</li>
     *     <li>{@code GROOVY}: Handle account changes and warnings via Groovy scripts</li>
     *     <li>{@code REJECT_RESULT_CODE}: Handle account state only if the ldap authentication result code isn't blocked</li>
     * </ul>
     */
    private PasswordPolicyHandlingOptions strategy = PasswordPolicyHandlingOptions.DEFAULT;

    /**
     * Key-value structure (Map) that indicates a list of boolean attributes as keys.
     * If either attribute value is true, indicating an account state is flagged,
     * the corresponding error can be thrown.
     * Example {@code accountLocked=javax.security.auth.login.AccountLockedException}
     */
    private Map<String, Class<LoginException>> policyAttributes = new LinkedCaseInsensitiveMap<>();

    /**
     * Whether password policy should be enabled.
     */
    private boolean enabled = true;

    /**
     * An implementation of a policy class that knows how to handle LDAP responses.
     * The class must be an implementation of {@code org.ldaptive.auth.AuthenticationResponseHandler}.
     */
    private String customPolicyClass;
    /**
     * When dealing with FreeIPA, indicates the number of allows login failures.
     */
    private int loginFailures = 5;

    /**
     * Used by an account state handling policy that only calculates account warnings
     * in case the LDAP entry carries an attribute {@link #warningAttributeName}
     * whose value matches this field.
     */
    private String warningAttributeValue;
    /**
     * Used by an account state handling policy that only calculates account warnings
     * in case the LDAP entry carries this attribute.
     */
    private String warningAttributeName;
    /**
     * Indicates if warning should be displayed, when the ldap attribute value
     * matches the {@link #warningAttributeValue}.
     */
    private boolean displayWarningOnMatch = true;

    /**
     * Always display the password expiration warning regardless.
     */
    private boolean warnAll;
    /**
     * In the event that AD is chosen as the type, this is used to calculate
     * a warning period to see if account expiry is within the calculated window.
     */
    private int warningDays = 30;
    /**
     * LDAP type. Accepted values are {@code GENERIC,AD,FreeIPA,EDirectory}
     */
    private AbstractLdapProperties.LdapType type = AbstractLdapProperties.LdapType.GENERIC;

    /**
     * Handle password policy via Groovy script.
     */
    private Groovy groovy = new Groovy();

    public AbstractLdapProperties.LdapType getType() {
        return type;
    }

    public String getCustomPolicyClass() {
        return customPolicyClass;
    }

    public void setCustomPolicyClass(final String customPolicyClass) {
        this.customPolicyClass = customPolicyClass;
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

    public PasswordPolicyHandlingOptions getStrategy() {
        return strategy;
    }

    public void setStrategy(final PasswordPolicyHandlingOptions strategy) {
        this.strategy = strategy;
    }

    public Groovy getGroovy() {
        return groovy;
    }

    public void setGroovy(final Groovy groovy) {
        this.groovy = groovy;
    }

    @RequiresModule(name = "cas-server-support-ldap")
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }
}
