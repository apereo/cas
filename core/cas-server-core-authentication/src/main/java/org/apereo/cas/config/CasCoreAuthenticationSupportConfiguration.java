package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationPolicyResolver;
import org.apereo.cas.authentication.AuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionFactory;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.GroovyAuthenticationPostProcessor;
import org.apereo.cas.authentication.GroovyAuthenticationPreProcessor;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.handler.ByCredentialSourceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.GroovyAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.policy.RegisteredServiceAuthenticationPolicyResolver;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.cache.DefaultPrincipalAttributesRepositoryCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import java.util.List;

/**
 * This is {@link CasCoreAuthenticationSupportConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication)
@Configuration(value = "CasCoreAuthenticationSupportConfiguration", proxyBeanMethods = false)
class CasCoreAuthenticationSupportConfiguration {

    @Configuration(value = "CasCoreAuthenticationPrincipalCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationPrincipalCacheConfiguration {

        @ConditionalOnMissingBean(name = PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
            return new DefaultPrincipalAttributesRepositoryCache();
        }
    }

    @Configuration(value = "CasCoreAuthenticationHandlerResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationHandlerResolverConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "registeredServiceAuthenticationHandlerResolver")
        public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan) {
            val resolver = new RegisteredServiceAuthenticationHandlerResolver(servicesManager, authenticationServiceSelectionPlan);
            resolver.setOrder(casProperties.getAuthn().getCore().getServiceAuthenticationResolution().getOrder());
            return resolver;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "groovyAuthenticationHandlerResolver")
        @ConditionalOnMissingGraalVMNativeImage
        public AuthenticationHandlerResolver groovyAuthenticationHandlerResolver(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) throws Exception {
            return BeanSupplier.of(AuthenticationHandlerResolver.class)
                .when(BeanCondition.on("cas.authn.core.groovy-authentication-resolution.location").exists()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val groovy = casProperties.getAuthn().getCore().getGroovyAuthenticationResolution();
                    return new GroovyAuthenticationHandlerResolver(groovy.getLocation(), servicesManager, groovy.getOrder());
                })
                .otherwise(AuthenticationHandlerResolver::noOp)
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "byCredentialSourceAuthenticationHandlerResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandlerResolver byCredentialSourceAuthenticationHandlerResolver(
            final ConfigurableApplicationContext applicationContext) throws Exception {
            return BeanSupplier.of(AuthenticationHandlerResolver.class)
                .when(BeanCondition.on("cas.authn.policy.source-selection-enabled").isTrue().given(applicationContext.getEnvironment()))
                .supply(ByCredentialSourceAuthenticationHandlerResolver::new)
                .otherwise(AuthenticationHandlerResolver::noOp)
                .get();
        }

    }

    @Configuration(value = "CasCoreAuthenticationSupportBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationSupportBaseConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = AuthenticationSystemSupport.BEAN_NAME)
        public AuthenticationSystemSupport defaultAuthenticationSystemSupport(
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final PrincipalFactory principalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("authenticationTransactionManager")
            final AuthenticationTransactionManager authenticationTransactionManager,
            @Qualifier(PrincipalElectionStrategy.BEAN_NAME)
            final PrincipalElectionStrategy principalElectionStrategy,
            @Qualifier("authenticationResultBuilderFactory")
            final AuthenticationResultBuilderFactory authenticationResultBuilderFactory,
            @Qualifier(AuthenticationTransactionFactory.BEAN_NAME)
            final AuthenticationTransactionFactory authenticationTransactionFactory,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(TenantsManager.BEAN_NAME)
            final TenantsManager tenantsManager) {
            return new DefaultAuthenticationSystemSupport(authenticationTransactionManager,
                principalElectionStrategy, authenticationResultBuilderFactory,
                authenticationTransactionFactory, servicesManager,
                defaultPrincipalResolver, principalFactory,
                tenantExtractor, tenantsManager);
        }
    }

    @Configuration(value = "CasCoreAuthenticationPolicyResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationPolicyResolverConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyResolver")
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
    static class CasCoreAuthenticationExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "authenticationHandlerResolversExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer authenticationHandlerResolversExecutionPlanConfigurer(
            @Qualifier("byCredentialSourceAuthenticationHandlerResolver")
            final ObjectProvider<@NonNull AuthenticationHandlerResolver> byCredentialSourceAuthenticationHandlerResolver,
            @Qualifier("registeredServiceAuthenticationHandlerResolver")
            final AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver,
            @Qualifier("registeredServiceAuthenticationPolicyResolver")
            final AuthenticationPolicyResolver registeredServiceAuthenticationPolicyResolver,
            @Qualifier("groovyAuthenticationHandlerResolver")
            final ObjectProvider<@NonNull AuthenticationHandlerResolver> groovyAuthenticationHandlerResolver) {
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
        @ConditionalOnMissingGraalVMNativeImage
        public AuthenticationEventExecutionPlanConfigurer groovyAuthenticationProcessorExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties) {
            return plan -> {
                val engine = casProperties.getAuthn().getCore().getEngine();
                val preResource = engine.getGroovyPreProcessor().getLocation();
                if (preResource != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                    plan.registerAuthenticationPreProcessor(new GroovyAuthenticationPreProcessor(preResource));
                }
                val postResource = engine.getGroovyPostProcessor().getLocation();
                if (postResource != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                    plan.registerAuthenticationPostProcessor(new GroovyAuthenticationPostProcessor(postResource));
                }
            };
        }
    }

    @Configuration(value = "CasCoreAuthenticationServiceSelectionConfiguration", proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    @Slf4j
    static class CasCoreAuthenticationServiceSelectionConfiguration {
        @ConditionalOnMissingBean(name = AuthenticationServiceSelectionPlan.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan(
            final List<AuthenticationServiceSelectionStrategyConfigurer> configurers) {
            val plan = new DefaultAuthenticationServiceSelectionPlan();
            configurers.forEach(c -> {
                LOGGER.trace("Configuring authentication request service selection strategy plan [{}]", c.getName());
                c.configureAuthenticationServiceSelectionStrategy(plan);
            });
            return plan;
        }
    }
}
