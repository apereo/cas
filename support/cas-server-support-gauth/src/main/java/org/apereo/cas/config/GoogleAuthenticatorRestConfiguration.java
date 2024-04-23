package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.gauth.rest.GoogleAuthenticatorRestHttpRequestCredentialFactory;
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
 * This is {@link GoogleAuthenticatorRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(RestHttpRequestCredentialFactoryConfigurer.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator)
@Configuration(value = "GoogleAuthenticatorRestConfiguration", proxyBeanMethods = false)
class GoogleAuthenticatorRestConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorRestHttpRequestCredentialFactoryConfigurer")
    public RestHttpRequestCredentialFactoryConfigurer googleAuthenticatorRestHttpRequestCredentialFactoryConfigurer(
        @Qualifier("googleAuthenticatorRestHttpRequestCredentialFactory")
        final RestHttpRequestCredentialFactory googleAuthenticatorRestHttpRequestCredentialFactory) {
        return factory -> factory.registerCredentialFactory(googleAuthenticatorRestHttpRequestCredentialFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorRestHttpRequestCredentialFactory")
    public RestHttpRequestCredentialFactory googleAuthenticatorRestHttpRequestCredentialFactory() {
        return new GoogleAuthenticatorRestHttpRequestCredentialFactory();
    }
}
