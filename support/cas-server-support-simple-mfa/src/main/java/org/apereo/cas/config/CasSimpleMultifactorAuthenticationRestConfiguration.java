package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.rest.CasSimpleMultifactorRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSimpleMultifactorAuthenticationRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(RestHttpRequestCredentialFactoryConfigurer.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationRestConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationRestConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestHttpRequestCredentialFactoryConfigurer")
    public RestHttpRequestCredentialFactoryConfigurer casSimpleMultifactorRestHttpRequestCredentialFactoryConfigurer(
        @Qualifier("casSimpleMultifactorRestHttpRequestCredentialFactory")
        final RestHttpRequestCredentialFactory casSimpleMultifactorRestHttpRequestCredentialFactory) {
        return factory -> factory.registerCredentialFactory(casSimpleMultifactorRestHttpRequestCredentialFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestHttpRequestCredentialFactory")
    public RestHttpRequestCredentialFactory casSimpleMultifactorRestHttpRequestCredentialFactory() {
        return new CasSimpleMultifactorRestHttpRequestCredentialFactory();
    }
}
