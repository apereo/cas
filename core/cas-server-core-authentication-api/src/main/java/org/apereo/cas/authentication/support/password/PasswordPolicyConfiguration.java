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

    private boolean alwaysDisplayPasswordExpirationWarning;
    private int passwordWarningNumberOfDays;
    private int loginFailures;

    public PasswordPolicyConfiguration(final boolean alwaysDisplayPasswordExpirationWarning,
                                       final int passwordWarningNumberOfDays,
                                       final int loginFailures) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
        this.passwordWarningNumberOfDays = passwordWarningNumberOfDays;
        this.loginFailures = loginFailures;
    }

    public PasswordPolicyConfiguration(final int passwordWarningNumberOfDays) {
        this.passwordWarningNumberOfDays = passwordWarningNumberOfDays;
    }

    public PasswordPolicyConfiguration(final PasswordPolicyProperties props) {
        this(props.isWarnAll(), props.getWarningDays(), props.getLoginFailures());
    }

    public PasswordPolicyConfiguration() {
    }

    public void setAlwaysDisplayPasswordExpirationWarning(final boolean alwaysDisplayPasswordExpirationWarning) {
        this.alwaysDisplayPasswordExpirationWarning = alwaysDisplayPasswordExpirationWarning;
    }

    public void setPasswordWarningNumberOfDays(final int passwordWarningNumberOfDays) {
        this.passwordWarningNumberOfDays = passwordWarningNumberOfDays;
    }

    public void setLoginFailures(final int loginFailures) {
        this.loginFailures = loginFailures;
    }

    public boolean isAlwaysDisplayPasswordExpirationWarning() {
        return alwaysDisplayPasswordExpirationWarning;
    }

    public int getPasswordWarningNumberOfDays() {
        return passwordWarningNumberOfDays;
    }

    public int getLoginFailures() {
        return loginFailures;
    }
}
