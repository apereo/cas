package org.apereo.cas.authentication.support.password;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordPolicyConfiguration {

    private final PasswordPolicyProperties passwordPolicyProperties;

    public PasswordPolicyConfiguration(final PasswordPolicyProperties passwordPolicyProperties) {
        this.passwordPolicyProperties = passwordPolicyProperties;
    }

    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.passwordPolicyProperties.isWarnAll();
    }
    
    public int getPasswordWarningNumberOfDays() {
        return this.passwordPolicyProperties.getWarningDays();
    }
    
    public int getLoginFailures() {
        return this.passwordPolicyProperties.getLoginFailures();
    }
}
