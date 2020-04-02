package org.apereo.cas.adaptors.authy.config.support.authentication;

import org.apereo.cas.adaptors.authy.AuthyTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link AuthyAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "authyAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthyAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "authyComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer authyComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(AuthyTokenCredential.class);
    }
}
