package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.surrogate.DefaultSurrogateCredentialParser;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.action.LoadSurrogatesListAction;
import org.apereo.cas.web.flow.action.SurrogateAuthorizationAction;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.action.SurrogateSelectionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SurrogateAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
@Configuration(value = "SurrogateAuthenticationWebflowConfiguration", proxyBeanMethods = false)
class SurrogateAuthenticationWebflowConfiguration {

    @Configuration(value = "SurrogateAuthenticationWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer surrogateWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new SurrogateWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry,
                applicationContext, casProperties);
        }
    }

    @Configuration(value = "SurrogateAuthenticationWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SELECT_SURROGATE_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action selectSurrogateAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME)
            final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SurrogateSelectionAction(surrogatePrincipalBuilder))
                .withId(CasWebflowConstants.ACTION_ID_SELECT_SURROGATE_ACTION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = SurrogateCredentialParser.BEAN_NAME)
        public SurrogateCredentialParser surrogateCredentialParser(final CasConfigurationProperties casProperties) {
            return new DefaultSurrogateCredentialParser(casProperties.getAuthn().getSurrogate());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
        public Action surrogateInitialAuthenticationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(SurrogateCredentialParser.BEAN_NAME)
            final SurrogateCredentialParser surrogateCredentialParser) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SurrogateInitialAuthenticationAction(surrogateCredentialParser))
                .withId(CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SURROGATE_AUTHORIZATION_CHECK)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action surrogateAuthorizationCheck(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SurrogateAuthorizationAction(registeredServiceAccessStrategyEnforcer))
                .withId(CasWebflowConstants.ACTION_ID_SURROGATE_AUTHORIZATION_CHECK)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOAD_SURROGATES_LIST_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action loadSurrogatesListAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
            final SurrogateAuthenticationService surrogateAuthenticationService,
            @Qualifier(SurrogateAuthenticationPrincipalBuilder.BEAN_NAME)
            final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new LoadSurrogatesListAction(surrogateAuthenticationService, surrogatePrincipalBuilder))
                .withId(CasWebflowConstants.ACTION_ID_LOAD_SURROGATES_LIST_ACTION)
                .build()
                .get();
        }
    }

    @Configuration(value = "SurrogateAuthenticationInitializerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationInitializerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public InitializingBean surrogateAuthenticationWebflowInitializer(
            @Qualifier(CasWebflowExceptionCatalog.BEAN_NAME)
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return () -> handledAuthenticationExceptions.registerException(SurrogateAuthenticationException.class);
        }
    }

    @Configuration(value = "SurrogateAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SurrogateAuthenticationWebflowPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "surrogateCasWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer surrogateCasWebflowExecutionPlanConfigurer(
            @Qualifier("surrogateWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer);
        }
    }
}
