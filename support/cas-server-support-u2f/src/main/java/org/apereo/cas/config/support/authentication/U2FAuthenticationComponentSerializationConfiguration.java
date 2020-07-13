package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.u2f.U2FTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link U2FAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "u2fAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "u2fComponentSerializationPlanConfigurer")
    @RefreshScope
    public ComponentSerializationPlanConfigurer u2fComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(U2FTokenCredential.class);
    }
}
