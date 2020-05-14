package org.apereo.cas.config;

import org.apereo.cas.adaptors.swivel.SwivelTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SwivelComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "swivelComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SwivelComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "swivelComponentSerializationPlanConfigurer")
    @RefreshScope
    public ComponentSerializationPlanConfigurer swivelComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(SwivelTokenCredential.class);
    }
}
