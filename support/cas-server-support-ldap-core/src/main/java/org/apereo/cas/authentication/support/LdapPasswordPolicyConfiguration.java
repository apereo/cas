package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;

/**
 * LDAP-specific password policy configuration container.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class LdapPasswordPolicyConfiguration extends PasswordPolicyConfiguration {

    /**
     * Directory-specific account state handler component.
     */
    private LdapAccountStateHandler accountStateHandler;

    public LdapPasswordPolicyConfiguration(final boolean alwaysDisplayPasswordExpirationWarning,
                                           final int passwordWarningNumberOfDays, final int loginFailures) {
        super(alwaysDisplayPasswordExpirationWarning, passwordWarningNumberOfDays, loginFailures);
    }

    public LdapPasswordPolicyConfiguration(final PasswordPolicyProperties props) {
       super(props);
    }

    public LdapPasswordPolicyConfiguration() {
    }

    /**
     * @return Account state handler component.
     */
    public LdapAccountStateHandler getAccountStateHandler() {
        return this.accountStateHandler;
    }
    
    public void setAccountStateHandler(final LdapAccountStateHandler accountStateHandler) {
        this.accountStateHandler = accountStateHandler;
    }
}
