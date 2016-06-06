package org.apereo.cas.authentication.support;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * LDAP-specific password policy configuration container.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class LdapPasswordPolicyConfiguration extends PasswordPolicyConfiguration {

    /** Directory-specific account state handler component. */
    private AccountStateHandler accountStateHandler;

    public LdapPasswordPolicyConfiguration(final PasswordPolicyProperties passwordPolicyProperties) {
        super(passwordPolicyProperties);
    }

    /**
     * @return  Account state handler component.
     */
    public AccountStateHandler getAccountStateHandler() {
        return this.accountStateHandler;
    }

    /**
     * Sets the directory-specific account state handler. If none is defined, account state handling is disabled,
     * which is the default behavior.
     *
     * @param accountStateHandler Account state handler.
     */
    @Autowired
    public void setAccountStateHandler(@Qualifier("accountStateHandler")
                                           final AccountStateHandler accountStateHandler) {
        this.accountStateHandler = accountStateHandler;
    }
}
