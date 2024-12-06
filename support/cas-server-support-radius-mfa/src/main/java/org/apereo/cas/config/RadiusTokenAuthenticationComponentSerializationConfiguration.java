package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link RadiusTokenAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.RadiusMFA)
@Configuration(value = "RadiusTokenAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
class RadiusTokenAuthenticationComponentSerializationConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.radius.client.inet-address");

    @Bean
    @ConditionalOnMissingBean(name = "radiusTokenComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer radiusTokenComponentSerializationPlanConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(ComponentSerializationPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerSerializableClass(RadiusTokenCredential.class))
            .otherwiseProxy()
            .get();
    }
}
