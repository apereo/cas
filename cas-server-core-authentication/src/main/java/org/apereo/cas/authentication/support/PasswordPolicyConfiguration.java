package org.apereo.cas.authentication.support;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordPolicyConfiguration {
    
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private PasswordPolicyProperties passwordPolicyProperties;

    public PasswordPolicyConfiguration(final PasswordPolicyProperties passwordPolicyProperties) {
        this.passwordPolicyProperties = passwordPolicyProperties;
    }

    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.passwordPolicyProperties.isWarnAll();
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyProperties.getUrl();
    }

    public int getPasswordWarningNumberOfDays() {
        return this.passwordPolicyProperties.getWarningDays();
    }
    
    public int getLoginFailures() {
        return this.passwordPolicyProperties.getLoginFailures();
    }
}
