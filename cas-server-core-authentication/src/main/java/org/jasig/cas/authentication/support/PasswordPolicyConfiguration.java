package org.jasig.cas.authentication.support;

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

    private static final int DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS = 30;

    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Disregard the warning period and warn all users of password expiration. */
    private boolean alwaysDisplayPasswordExpirationWarning;

    /** Threshold number of days till password expiration below which a warning is displayed. **/
    private int passwordWarningNumberOfDays = DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS;

    /** Url to the password policy application. **/
    private String passwordPolicyUrl;


    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.alwaysDisplayPasswordExpirationWarning;
    }

    public void setAlwaysDisplayPasswordExpirationWarning(final boolean alwaysDisplayPasswordExpirationWarning) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }

    public void setPasswordPolicyUrl(final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }

    public int getPasswordWarningNumberOfDays() {
        return passwordWarningNumberOfDays;
    }

    public void setPasswordWarningNumberOfDays(final int days) {
        this.passwordWarningNumberOfDays = days;
    }
}
