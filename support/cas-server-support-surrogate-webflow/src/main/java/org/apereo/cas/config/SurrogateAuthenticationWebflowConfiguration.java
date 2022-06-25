package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanSupplier;
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
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link SurrogateAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class SurrogateAuthenticationWebflowConfiguration {

    @Configuration(value = "SurrogateAuthenticationWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationWebflowBaseConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnClass(DuoSecurityAuthenticationService.class)
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
        public CasMultifactorWebflowCustomizer surrogateCasMultifactorWebflowCustomizer(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasMultifactorWebflowCustomizer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new CasMultifactorWebflowCustomizer() {
                    @Override
                    public List<String> getWebflowAttributeMappings() {
                        return List.of(WebUtils.REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE);
                    }
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer surrogateWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new SurrogateWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnClass(DuoSecurityAuthenticationService.class)
        @ConditionalOnMissingBean(name = "surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
        public CasWebflowConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new SurrogateWebflowConfigurer.DuoSecurityMultifactorAuthenticationWebflowConfigurer(
                    flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "SurrogateAuthenticationWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SELECT_SURROGATE_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action selectSurrogateAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("surrogatePrincipalBuilder")
            final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
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
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
        public Action surrogateInitialAuthenticationAction(final CasConfigurationProperties casProperties) {
            return new SurrogateInitialAuthenticationAction(casProperties.getAuthn().getSurrogate().getSeparator());
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
            @Qualifier("surrogatePrincipalBuilder")
            final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
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
    public static class SurrogateAuthenticationInitializerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public InitializingBean surrogateAuthenticationWebflowInitializer(
            @Qualifier("handledAuthenticationExceptions")
            final CasWebflowExceptionCatalog handledAuthenticationExceptions) {
            return () -> handledAuthenticationExceptions.registerException(SurrogateAuthenticationException.class);
        }
    }

    @Configuration(value = "SurrogateAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationWebflowPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "surrogateCasWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer surrogateCasWebflowExecutionPlanConfigurer(
            @Qualifier("surrogateWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer);
        }
    }

    @Configuration(value = "SurrogateAuthenticationDuoSecurityWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
    @ConditionalOnClass(DuoSecurityAuthenticationService.class)
    public static class SurrogateAuthenticationDuoSecurityWebflowPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "surrogateDuoSecurityMultifactorAuthenticationWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
