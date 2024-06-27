package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.passwordless.SurrogateDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationPreProcessor;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationRequestParser;
import org.pac4j.core.context.session.SessionStore;
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
@ConditionalOnClass(CasPasswordlessAuthenticationWebflowAutoConfiguration.class)
@Configuration(value = "SurrogateAuthenticationPasswordlessConfiguration", proxyBeanMethods = false)
class SurrogateAuthenticationPasswordlessConfiguration {

    @Configuration(value = "BaseSurrogateAuthenticationPasswordlessConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = {
        CasFeatureModule.FeatureCatalog.PasswordlessAuthn,
        CasFeatureModule.FeatureCatalog.SurrogateAuthentication
    })
    public static class BaseSurrogateAuthenticationPasswordlessConfiguration {
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

    @Configuration(value = "SurrogateAuthenticationDelegationPasswordlessConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(CasDelegatedAuthenticationAutoConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = {
        CasFeatureModule.FeatureCatalog.PasswordlessAuthn,
        CasFeatureModule.FeatureCatalog.DelegatedAuthentication,
        CasFeatureModule.FeatureCatalog.SurrogateAuthentication
    })
    public static class SurrogateAuthenticationDelegationPasswordlessConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "surrogateDelegatedPasswordlessAuthenticationCredentialExtractor")
        public DelegatedAuthenticationCredentialExtractor surrogateDelegatedPasswordlessAuthenticationCredentialExtractor(
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return new SurrogateDelegatedAuthenticationCredentialExtractor(delegatedClientDistributedSessionStore);
        }
    }
}
