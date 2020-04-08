package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.config.cond.ConditionalOnDuoSecurityConfigured;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DuoSecurityComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "duoSecurityComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnDuoSecurityConfigured
public class DuoSecurityComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer duoSecurityComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(DuoSecurityCredential.class);
            plan.registerSerializableClass(DuoSecurityDirectCredential.class);
        };
    }
}
