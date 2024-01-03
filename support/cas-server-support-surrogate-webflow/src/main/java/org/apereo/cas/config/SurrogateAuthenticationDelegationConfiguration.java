package org.apereo.cas.config;

import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.pac4j.SurrogateDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.web.flow.pac4j.SurrogateDelegatedAuthenticationPreProcessor;

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
 * This is {@link SurrogateAuthenticationDelegationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.DelegatedAuthentication,
    CasFeatureModule.FeatureCatalog.SurrogateAuthentication
})
@ConditionalOnClass(DelegatedAuthenticationWebflowConfiguration.class)
@Configuration(proxyBeanMethods = false)
class SurrogateAuthenticationDelegationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "surrogateDelegatedAuthenticationPreProcessor")
    public DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor(
        @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
        final SurrogateAuthenticationService surrogateAuthenticationService,
        @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME) final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder) {
        return new SurrogateDelegatedAuthenticationPreProcessor(surrogateAuthenticationService, surrogatePrincipalBuilder);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "surrogateDelegatedAuthenticationCredentialExtractor")
    public DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor(
        @Qualifier("delegatedClientDistributedSessionStore")
        final SessionStore delegatedClientDistributedSessionStore) {
        return new SurrogateDelegatedAuthenticationCredentialExtractor(delegatedClientDistributedSessionStore);
    }
}
