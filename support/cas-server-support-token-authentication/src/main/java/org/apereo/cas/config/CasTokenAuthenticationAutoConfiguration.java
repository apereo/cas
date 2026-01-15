package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.authentication.OidcTokenAuthenticationHandler;
import org.apereo.cas.token.authentication.TokenAuthenticationHandler;
import org.apereo.cas.token.authentication.TokenAuthenticationPostProcessor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasTokenAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "token")
@AutoConfiguration
@ImportAutoConfiguration(CasTokenCoreAutoConfiguration.class)
public class CasTokenAuthenticationAutoConfiguration {

    @Configuration(value = "TokenAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TokenAuthenticationConfiguration {
        @ConditionalOnMissingBean(name = "tokenPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory tokenPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @ConditionalOnMissingBean(name = "tokenAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler tokenAuthenticationHandler(
            @Qualifier("tokenCipherExecutor")
            final CipherExecutor tokenCipherExecutor,
            final CasConfigurationProperties casProperties,
            @Qualifier("tokenPrincipalFactory")
            final PrincipalFactory tokenPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val token = casProperties.getAuthn().getToken();
            val handler = new TokenAuthenticationHandler(servicesManager, tokenPrincipalFactory, token);
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(token.getCredentialCriteria()));
            handler.setState(token.getState());
            return handler;
        }

        @ConditionalOnMissingBean(name = "tokenAuthenticationPostProcessor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationPostProcessor tokenAuthenticationPostProcessor(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return casProperties.getAuthn().getToken().isSsoTokenEnabled()
                ? new TokenAuthenticationPostProcessor(servicesManager, registeredServiceAccessStrategyEnforcer)
                : AuthenticationPostProcessor.none();
        }

        @ConditionalOnMissingBean(name = "tokenAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer tokenAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("tokenAuthenticationPostProcessor")
            final AuthenticationPostProcessor tokenAuthenticationPostProcessor,
            @Qualifier("tokenAuthenticationHandler")
            final AuthenticationHandler tokenAuthenticationHandler,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> {
                plan.registerAuthenticationHandlerWithPrincipalResolver(tokenAuthenticationHandler, defaultPrincipalResolver);
                plan.registerAuthenticationPostProcessor(tokenAuthenticationPostProcessor);
            };
        }
    }

    @Configuration(value = "TokenOidcAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(CasOidcAutoConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
    static class TokenOidcAuthenticationConfiguration {
        
        @ConditionalOnMissingBean(name = "oidcTokenAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler oidcTokenAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("tokenPrincipalFactory")
            final PrincipalFactory tokenPrincipalFactory) {
            val token = casProperties.getAuthn().getToken();
            val handler = new OidcTokenAuthenticationHandler(tokenPrincipalFactory, oidcConfigurationContext, token);
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(token.getCredentialCriteria()));
            handler.setState(token.getState());
            return handler;
        }

        @ConditionalOnMissingBean(name = "oidcTokenAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer oidcTokenAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("oidcTokenAuthenticationHandler")
            final AuthenticationHandler oidcTokenAuthenticationHandler,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(oidcTokenAuthenticationHandler, defaultPrincipalResolver);
        }
    }
}
