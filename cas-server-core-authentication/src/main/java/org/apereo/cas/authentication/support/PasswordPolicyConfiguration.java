package org.apereo.cas.authentication.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Disregard the warning period and warn all users of password expiration.
     */
    @Value("${password.policy.warnAll:false}")
    private boolean alwaysDisplayPasswordExpirationWarning;

    /**
     * Threshold number of days till password expiration below which a warning is displayed.
     **/
    @Value("${password.policy.warningDays:" + DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS + '}')
    private int passwordWarningNumberOfDays = DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS;

    /**
     * Url to the password policy application.
     **/
    @Value("${password.policy.url:https://password.example.edu/change}")
    private String passwordPolicyUrl;

    public PasswordPolicyConfiguration() {
    }

    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.alwaysDisplayPasswordExpirationWarning;
    }
    
    public void setAlwaysDisplayPasswordExpirationWarning(
            final boolean alwaysDisplayPasswordExpirationWarning) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }
    
    public void setPasswordPolicyUrl(final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }

    public int getPasswordWarningNumberOfDays() {
        return this.passwordWarningNumberOfDays;
    }
    
    public void setPasswordWarningNumberOfDays(final int days) {
        this.passwordWarningNumberOfDays = days;
    }
}
