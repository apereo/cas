package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.rest.SurrogateAuthenticationRestHttpRequestCredentialFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateAuthenticationRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(RestHttpRequestCredentialFactoryConfigurer.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
@Configuration(value = "SurrogateAuthenticationRestConfiguration", proxyBeanMethods = false)
class SurrogateAuthenticationRestConfiguration {

    /**
     * Override the core bean definition
     * that handles username+password to
     * avoid duplicate authentication attempts.
     *
     * @param surrogateAuthenticationService the surrogate authentication service
     * @param casProperties                  the cas properties
     * @return configurer instance
     */
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RestHttpRequestCredentialFactoryConfigurer restHttpRequestCredentialFactoryConfigurer(
        @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
        final SurrogateAuthenticationService surrogateAuthenticationService, final CasConfigurationProperties casProperties) {
        return factory -> factory.registerCredentialFactory(
            new SurrogateAuthenticationRestHttpRequestCredentialFactory(surrogateAuthenticationService, casProperties.getAuthn().getSurrogate()));
    }
}
