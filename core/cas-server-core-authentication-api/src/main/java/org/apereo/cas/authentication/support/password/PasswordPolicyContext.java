package org.apereo.cas.authentication.support.password;

import org.apereo.cas.authentication.AuthenticationAccountStateHandler;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Container for password policy configuration.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Setter
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class PasswordPolicyContext {

    /**
     * Directory-specific account state handler component.
     */
    private AuthenticationAccountStateHandler accountStateHandler;

    private boolean alwaysDisplayPasswordExpirationWarning;
    private int passwordWarningNumberOfDays = 30;
    private int loginFailures = 5;

    public PasswordPolicyContext(final int passwordWarningNumberOfDays) {
        this.passwordWarningNumberOfDays = passwordWarningNumberOfDays;
    }

    public PasswordPolicyContext(final PasswordPolicyProperties props) {
        this(null, props.isWarnAll(), props.getWarningDays(), props.getLoginFailures());
    }
}
