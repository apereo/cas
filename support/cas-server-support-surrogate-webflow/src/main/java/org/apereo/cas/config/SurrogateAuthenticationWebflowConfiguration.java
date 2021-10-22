package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.config.cond.ConditionalOnDuoSecurityConfigured;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.action.LoadSurrogatesListAction;
import org.apereo.cas.web.flow.action.SurrogateAuthorizationAction;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.action.SurrogateSelectionAction;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Configuration(value = "surrogateAuthenticationWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateAuthenticationWebflowConfiguration {

    @Configuration(value = "SurrogateAuthenticationWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationWebflowBaseConfiguration {

        @Bean
        @ConditionalOnDuoSecurityConfigured
        public CasMultifactorWebflowCustomizer surrogateCasMultifactorWebflowCustomizer() {
            return new CasMultifactorWebflowCustomizer() {
                @Override
                public List<String> getMultifactorWebflowAttributeMappings() {
                    return List.of(WebUtils.REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE);
                }
            };
        }

        @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
        @Bean
        @Autowired
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
        @Autowired
        @ConditionalOnDuoSecurityConfigured
        @ConditionalOnMissingBean(name = "surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
        public CasWebflowConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new SurrogateWebflowConfigurer.DuoSecurityMultifactorAuthenticationWebflowConfigurer(
                flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        }
    }

    @Configuration(value = "SurrogateAuthenticationWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SELECT_SURROGATE_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action selectSurrogateAction(
            @Qualifier("surrogatePrincipalBuilder")
            final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
            return new SurrogateSelectionAction(surrogatePrincipalBuilder);
        }

        @Bean
        @Autowired
        public Action authenticationViaFormAction(
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier("adaptiveAuthenticationPolicy")
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier("serviceTicketRequestWebflowEventResolver")
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver, final CasConfigurationProperties casProperties) {
            return new SurrogateInitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
                casProperties.getAuthn().getSurrogate().getSeparator());
        }

        @ConditionalOnMissingBean(name = "surrogateAuthorizationCheck")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action surrogateAuthorizationCheck(
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return new SurrogateAuthorizationAction(registeredServiceAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOAD_SURROGATES_LIST_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action loadSurrogatesListAction(
            @Qualifier("surrogateAuthenticationService")
            final SurrogateAuthenticationService surrogateAuthenticationService,
            @Qualifier("surrogatePrincipalBuilder")
            final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
            return new LoadSurrogatesListAction(surrogateAuthenticationService, surrogatePrincipalBuilder);
        }
    }

    @Configuration(value = "SurrogateAuthenticationInitializerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationInitializerConfiguration {

        @Bean
        @Autowired
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
        @Autowired
        public CasWebflowExecutionPlanConfigurer surrogateCasWebflowExecutionPlanConfigurer(
            @Qualifier("surrogateWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer);
        }
    }

    @Configuration(value = "SurrogateAuthenticationDuoSecurityWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnDuoSecurityConfigured
    public static class SurrogateAuthenticationDuoSecurityWebflowPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "surrogateDuoSecurityMultifactorAuthenticationWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowExecutionPlanConfigurer(
            @Qualifier("surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer);
        }
    }
}
