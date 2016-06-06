package org.apereo.cas.config;

import org.apereo.cas.authentication.support.AccountStateHandler;
import org.apereo.cas.authentication.support.DefaultAccountStateHandler;
import org.apereo.cas.authentication.support.LdapPasswordPolicyConfiguration;
import org.apereo.cas.authentication.support.OptionalWarningAccountStateHandler;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapCoreConfiguration")
public class LdapCoreConfiguration {

    @Autowired
    private PasswordPolicyProperties passwordPolicyProperties;
    
    @Bean
    public AccountStateHandler accountStateHandler() {
        return new DefaultAccountStateHandler();
    }
    
    @Bean
    public PasswordPolicyConfiguration ldapPasswordPolicyConfiguration() {
        return new LdapPasswordPolicyConfiguration(this.passwordPolicyProperties);
    }

    @Bean
    @RefreshScope
    public AccountStateHandler optionalWarningAccountStateHandler() {
        return new OptionalWarningAccountStateHandler();
    }
}
