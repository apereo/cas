package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link WsFederationAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "wsFederationAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WsFederationAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "wsFederationAuthenticationComponentSerializationPlanConfigurer")
    @RefreshScope
    public ComponentSerializationPlanConfigurer wsFederationAuthenticationComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(WsFederationCredential.class);
    }
}
