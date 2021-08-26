package org.apereo.cas.config.support.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.rest.GoogleAuthenticatorRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link GoogleAuthenticatorRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "googleAuthenticatorRestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(value = RestHttpRequestCredentialFactoryConfigurer.class)
public class GoogleAuthenticatorRestConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorRestHttpRequestCredentialFactoryConfigurer")
    @Autowired
    public RestHttpRequestCredentialFactoryConfigurer googleAuthenticatorRestHttpRequestCredentialFactoryConfigurer(
        @Qualifier("googleAuthenticatorRestHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory googleAuthenticatorRestHttpRequestCredentialFactory) {
        return factory -> factory.registerCredentialFactory(googleAuthenticatorRestHttpRequestCredentialFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorRestHttpRequestCredentialFactory")
    public RestHttpRequestCredentialFactory googleAuthenticatorRestHttpRequestCredentialFactory() {
        return new GoogleAuthenticatorRestHttpRequestCredentialFactory();
    }
}
