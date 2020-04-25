package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.LdapPasswordSynchronizationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * This is {@link LdapPasswordSynchronizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "ldapPasswordSynchronizationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapPasswordSynchronizationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(destroyMethod = "closeSearchFactory")
    @Scope("prototype")
    public LdapPasswordSynchronizationAuthenticationPostProcessor ldapPasswordSynchronizationAuthenticationPostProcessor(
            final AbstractLdapSearchProperties properties) {
        return new LdapPasswordSynchronizationAuthenticationPostProcessor(properties);
    }

    @ConditionalOnMissingBean(name = "ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer ldapPasswordSynchronizationAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val ldap = casProperties.getAuthn().getPasswordSync().getLdap();
            ldap.stream()
                .filter(LdapPasswordSynchronizationProperties::isEnabled)
                .forEach(instance ->
                    plan.registerAuthenticationPostProcessor(
                        this.applicationContext.getBean(LdapPasswordSynchronizationAuthenticationPostProcessor.class, instance))
                );
        };
    }
}
