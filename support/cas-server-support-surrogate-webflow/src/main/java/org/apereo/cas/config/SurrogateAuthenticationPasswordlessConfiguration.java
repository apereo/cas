package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationPreProcessor;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationRequestParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateAuthenticationPasswordlessConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.PasswordlessAuthn,
    CasFeatureModule.FeatureCatalog.SurrogateAuthentication
})
@ConditionalOnClass(PasswordlessAuthenticationWebflowAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
class SurrogateAuthenticationPasswordlessConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "surrogatePasswordlessAuthenticationPreProcessor")
    public PasswordlessAuthenticationPreProcessor surrogatePasswordlessAuthenticationPreProcessor(
        @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
        final SurrogateAuthenticationService surrogateAuthenticationService,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME) final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder) {
        return new SurrogatePasswordlessAuthenticationPreProcessor(servicesManager, surrogatePrincipalBuilder, surrogateAuthenticationService);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "surrogatePasswordlessRequestParser")
    public PasswordlessRequestParser passwordlessRequestParser(
        @Qualifier(SurrogateCredentialParser.BEAN_NAME) final SurrogateCredentialParser surrogateCredentialParser) {
        return new SurrogatePasswordlessAuthenticationRequestParser(surrogateCredentialParser);
    }
}
