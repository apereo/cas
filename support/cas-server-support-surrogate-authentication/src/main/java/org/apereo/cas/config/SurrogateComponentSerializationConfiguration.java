package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy;
import org.apereo.cas.ticket.expiration.SurrogateSessionExpirationPolicy;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
@Configuration(value = "SurrogateComponentSerializationConfiguration", proxyBeanMethods = false)
class SurrogateComponentSerializationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "surrogateComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer surrogateComponentSerializationPlanConfigurer() {
        return plan -> {
            plan.registerSerializableClass(SurrogateSessionExpirationPolicy.class);
            plan.registerSerializableClass(SurrogateRegisteredServiceAccessStrategy.class);
        };
    }
}
