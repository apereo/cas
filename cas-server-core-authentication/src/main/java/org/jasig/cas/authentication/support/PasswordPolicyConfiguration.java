package org.jasig.cas.authentication.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("defaultPasswordPolicyConfiguration")
public class PasswordPolicyConfiguration {

    private static final int DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS = 30;

    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Disregard the warning period and warn all users of password expiration. */
    private boolean alwaysDisplayPasswordExpirationWarning;

    /** Threshold number of days till password expiration below which a warning is displayed. **/
    private int passwordWarningNumberOfDays = DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS;

    /** Url to the password policy application. **/
    private String passwordPolicyUrl;


    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return this.alwaysDisplayPasswordExpirationWarning;
    }


    @Autowired
    public void setAlwaysDisplayPasswordExpirationWarning(@Value("${password.policy.warnAll:false}")
                                                              final boolean alwaysDisplayPasswordExpirationWarning) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }

    @Autowired
    public void setPasswordPolicyUrl(@Value("${password.policy.url:https://password.example.edu/change}")
                                         final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }

    public int getPasswordWarningNumberOfDays() {
        return passwordWarningNumberOfDays;
    }

    @Autowired
    public void setPasswordWarningNumberOfDays(
            @Value("${password.policy.warningDays:" + DEFAULT_PASSWORD_WARNING_NUMBER_OF_DAYS + '}') final int days) {
        this.passwordWarningNumberOfDays = days;
    }
}
