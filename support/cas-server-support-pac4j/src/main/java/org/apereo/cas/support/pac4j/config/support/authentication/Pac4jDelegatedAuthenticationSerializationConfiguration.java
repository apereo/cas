package org.apereo.cas.support.pac4j.config.support.authentication;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link Pac4jDelegatedAuthenticationSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "pac4jDelegatedAuthenticationSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jDelegatedAuthenticationSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "pac4jComponentSerializationPlanConfigurer")
    @RefreshScope
    public ComponentSerializationPlanConfigurer pac4jComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(ClientCredential.class);
    }
}
