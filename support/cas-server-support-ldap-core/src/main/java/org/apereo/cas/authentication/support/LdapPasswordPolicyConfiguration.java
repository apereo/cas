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
    private AccountStateHandler accountStateHandler;

    public LdapPasswordPolicyConfiguration(final PasswordPolicyProperties passwordPolicyProperties) {
        super(passwordPolicyProperties);
    }

    /**
     * @return Account state handler component.
     */
    public AccountStateHandler getAccountStateHandler() {
        return this.accountStateHandler;
    }
    
    public void setAccountStateHandler(final AccountStateHandler accountStateHandler) {
        this.accountStateHandler = accountStateHandler;
    }
}
