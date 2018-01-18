package org.apereo.cas.authentication.support.password;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Setter
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class PasswordPolicyConfiguration {

    private boolean alwaysDisplayPasswordExpirationWarning;

    private int passwordWarningNumberOfDays;

    private int loginFailures;

    public PasswordPolicyConfiguration(final int passwordWarningNumberOfDays) {
        this.passwordWarningNumberOfDays = passwordWarningNumberOfDays;
    }

    public PasswordPolicyConfiguration(final PasswordPolicyProperties props) {
        this(props.isWarnAll(), props.getWarningDays(), props.getLoginFailures());
    }
}
