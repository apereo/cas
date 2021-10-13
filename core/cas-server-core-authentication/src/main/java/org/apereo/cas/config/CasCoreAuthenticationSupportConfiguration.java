package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationPolicyResolver;
import org.apereo.cas.authentication.AuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionFactory;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.GroovyAuthenticationPostProcessor;
import org.apereo.cas.authentication.GroovyAuthenticationPreProcessor;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.handler.ByCredentialSourceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.GroovyAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.policy.RegisteredServiceAuthenticationPolicyResolver;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.cache.DefaultPrincipalAttributesRepositoryCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreAuthenticationSupportConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreAuthenticationSupportConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreServicesConfiguration.class)
public class CasCoreAuthenticationSupportConfiguration {

    @Configuration(value = "CasCoreAuthenticationPrincipalCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationPrincipalCacheConfiguration {

        @ConditionalOnMissingBean(name = PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME)
        @Bean
        public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
            return new DefaultPrincipalAttributesRepositoryCache();
        }
    }

    @Configuration(value = "CasCoreAuthenticationHandlerResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationHandlerResolverConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "registeredServiceAuthenticationHandlerResolver")
        public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan) {
            val resolver = new RegisteredServiceAuthenticationHandlerResolver(servicesManager,
                authenticationServiceSelectionPlan);
            resolver.setOrder(casProperties.getAuthn().getCore().getServiceAuthenticationResolution().getOrder());
            return resolver;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "groovyAuthenticationHandlerResolver")
        @ConditionalOnProperty(name = "cas.authn.core.groovy-authentication-resolution.location")
        public AuthenticationHandlerResolver groovyAuthenticationHandlerResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val groovy = casProperties.getAuthn().getCore().getGroovyAuthenticationResolution();
            return new GroovyAuthenticationHandlerResolver(groovy.getLocation(), servicesManager, groovy.getOrder());
        }

        @Bean
        @ConditionalOnMissingBean(name = "byCredentialSourceAuthenticationHandlerResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnProperty(prefix = "cas.authn.policy", name = "source-selection-enabled", havingValue = "true")
        public AuthenticationHandlerResolver byCredentialSourceAuthenticationHandlerResolver() {
            return new ByCredentialSourceAuthenticationHandlerResolver();
        }

    }

    @Configuration(value = "CasCoreAuthenticationSupportBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationSupportBaseConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = AuthenticationSystemSupport.BEAN_NAME)
        @Autowired
        public AuthenticationSystemSupport defaultAuthenticationSystemSupport(
            @Qualifier("authenticationTransactionManager")
            final AuthenticationTransactionManager authenticationTransactionManager,
            @Qualifier("principalElectionStrategy")
            final PrincipalElectionStrategy principalElectionStrategy,
            @Qualifier("authenticationResultBuilderFactory")
            final AuthenticationResultBuilderFactory authenticationResultBuilderFactory,
            @Qualifier("authenticationTransactionFactory")
            final AuthenticationTransactionFactory authenticationTransactionFactory) {
            return new DefaultAuthenticationSystemSupport(authenticationTransactionManager,
                principalElectionStrategy, authenticationResultBuilderFactory,
                authenticationTransactionFactory);
        }
    }

    @Configuration(value = "CasCoreAuthenticationPolicyResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationPolicyResolverConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyResolver")
        @Autowired
        public AuthenticationPolicyResolver registeredServiceAuthenticationPolicyResolver(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan) {
            return new RegisteredServiceAuthenticationPolicyResolver(servicesManager,
                authenticationServiceSelectionPlan);
        }
    }

    @Configuration(value = "CasCoreAuthenticationExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "authenticationHandlerResolversExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationEventExecutionPlanConfigurer authenticationHandlerResolversExecutionPlanConfigurer(
            @Qualifier("byCredentialSourceAuthenticationHandlerResolver")
            final ObjectProvider<AuthenticationHandlerResolver> byCredentialSourceAuthenticationHandlerResolver,
            @Qualifier("registeredServiceAuthenticationHandlerResolver")
            final AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver,
            @Qualifier("registeredServiceAuthenticationPolicyResolver")
            final AuthenticationPolicyResolver registeredServiceAuthenticationPolicyResolver,
            @Qualifier("groovyAuthenticationHandlerResolver")
            final ObjectProvider<AuthenticationHandlerResolver> groovyAuthenticationHandlerResolver) {
            return plan -> {
                byCredentialSourceAuthenticationHandlerResolver.ifAvailable(plan::registerAuthenticationHandlerResolver);
                plan.registerAuthenticationHandlerResolver(registeredServiceAuthenticationHandlerResolver);
                plan.registerAuthenticationPolicyResolver(registeredServiceAuthenticationPolicyResolver);
                groovyAuthenticationHandlerResolver.ifAvailable(plan::registerAuthenticationHandlerResolver);
            };
        }

        @ConditionalOnMissingBean(name = "groovyAuthenticationProcessorExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationEventExecutionPlanConfigurer groovyAuthenticationProcessorExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties) {
            return plan -> {
                val engine = casProperties.getAuthn().getCore().getEngine();
                val preResource = engine.getGroovyPreProcessor().getLocation();
                if (preResource != null) {
                    plan.registerAuthenticationPreProcessor(new GroovyAuthenticationPreProcessor(preResource));
                }
                val postResource = engine.getGroovyPostProcessor().getLocation();
                if (postResource != null) {
                    plan.registerAuthenticationPostProcessor(new GroovyAuthenticationPostProcessor(postResource));
                }
            };
        }
    }

}
