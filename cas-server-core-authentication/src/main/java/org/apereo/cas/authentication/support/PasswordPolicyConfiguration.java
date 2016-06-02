package org.apereo.cas.authentication.support;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordPolicyConfiguration {

    private static final int DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS = 30;
    
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private PasswordPolicyProperties passwordPolicyProperties;

    public PasswordPolicyConfiguration(PasswordPolicyProperties passwordPolicyProperties) {
        this.passwordPolicyProperties = passwordPolicyProperties;
    }

    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.passwordPolicyProperties.getWarnAll();
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyProperties.getUrl();
    }

    public int getPasswordWarningNumberOfDays() {
        return this.passwordPolicyProperties.getWarningDays();
    }
}
