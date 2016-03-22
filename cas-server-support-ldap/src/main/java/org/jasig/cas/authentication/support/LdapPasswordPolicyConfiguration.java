package org.jasig.cas.authentication.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * LDAP-specific password policy configuration container.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RefreshScope
@Component("ldapPasswordPolicyConfiguration")
public class LdapPasswordPolicyConfiguration extends PasswordPolicyConfiguration {

    /** Directory-specific account state handler component. */
    
    private AccountStateHandler accountStateHandler;


    /**
     * @return  Account state handler component.
     */
    public AccountStateHandler getAccountStateHandler() {
        return accountStateHandler;
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
