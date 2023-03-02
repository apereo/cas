package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.GroovyDelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.client.ChainingDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DefaultDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.pac4j.client.GroovyDelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.GroovyDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.authz.DefaultDelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpoint;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.DelegatedAuthenticationCookieGenerator;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasFlowHandlerMapping;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DefaultDelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DefaultDelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.DefaultDelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DelegatedAuthenticationSingleSignOnEvaluator;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationGroovyPostProcessor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationPostProcessor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationClientRetryAction;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationGenerateClientsAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationCredentialSelectionAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationCredentialSelectionFinalizeAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationFailureAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationRedirectAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationStoreWebflowStateAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationClientFinishLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationClientLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationIdentityProviderFinalizeLogoutAction;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationIdentityProviderLogoutAction;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.controller.DefaultDelegatedAuthenticationNavigationController;
import org.apereo.cas.web.flow.error.DefaultDelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.web.flow.error.DelegatedAuthenticationErrorViewResolver;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DelegatedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@AutoConfiguration
public class DelegatedAuthenticationWebflowConfiguration {
    @Configuration(value = "DelegatedAuthenticationWebflowErrorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class, WebMvcProperties.class})
    public static class DelegatedAuthenticationWebflowErrorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "pac4jErrorViewResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ErrorViewResolver pac4jErrorViewResolver(
            @Qualifier(DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME) final DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator,
            final WebProperties webProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new DelegatedAuthenticationErrorViewResolver(applicationContext,
                webProperties.getResources(), delegatedClientAuthenticationFailureEvaluator);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowExecutionPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "delegatedCasWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer delegatedCasWebflowExecutionPlanConfigurer(
            @Qualifier("delegatedAuthenticationWebflowConfigurer") final CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(delegatedAuthenticationWebflowConfigurer);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowPlanConfiguration {
        @ConditionalOnMissingBean(name = "delegatedAuthenticationWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("delegatedClientRedirectFlowRegistry") final FlowDefinitionRegistry delegatedClientRedirectFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry logoutFlowDefinitionRegistry) {
            return new DelegatedAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                logoutFlowDefinitionRegistry, delegatedClientRedirectFlowRegistry, applicationContext, casProperties);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowManagementConfiguration {
        @ConditionalOnMissingBean(name = DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
            return new DefaultDelegatedClientAuthenticationWebflowManager(delegatedClientAuthenticationConfigurationContext);
        }

        @ConditionalOnMissingBean(name = "defaultDelegatedClientAuthenticationWebflowStateContributor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public DelegatedClientAuthenticationWebflowStateContributor defaultDelegatedClientAuthenticationWebflowStateContributor(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
            return new DefaultDelegatedClientAuthenticationWebflowStateContributor(delegatedClientAuthenticationConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedAuthenticationCasMultifactorWebflowCustomizer")
        public CasMultifactorWebflowCustomizer delegatedAuthenticationCasMultifactorWebflowCustomizer() {
            return new CasMultifactorWebflowCustomizer() {
                @Override
                public Collection<String> getCandidateStatesForMultifactorAuthentication() {
                    return List.of(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
                }
            };
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowClientConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "groovyDelegatedClientAuthenticationCredentialResolver")
        public DelegatedClientAuthenticationCredentialResolver groovyDelegatedClientAuthenticationCredentialResolver(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext configContext) {
            return BeanSupplier.of(DelegatedClientAuthenticationCredentialResolver.class)
                .when(BeanCondition.on("cas.authn.pac4j.profile-selection.groovy.location")
                    .exists().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val resource = casProperties.getAuthn().getPac4j().getProfileSelection().getGroovy().getLocation();
                    return new GroovyDelegatedClientAuthenticationCredentialResolver(configContext, resource);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderAuthorizer")
        public DelegatedClientIdentityProviderAuthorizer delegatedClientIdentityProviderAuthorizer(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS) final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer) {
            return new DefaultDelegatedClientIdentityProviderAuthorizer(servicesManager,
                registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer);
        }

        @Bean
        @ConditionalOnMissingBean(name = DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext configContext) {
            return new DefaultDelegatedClientAuthenticationFailureEvaluator(configContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderConfigurationPostProcessor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DelegatedClientIdentityProviderConfigurationPostProcessor.class)
                .when(BeanCondition.on("cas.authn.pac4j.core.groovy-provider-post-processor.location")
                    .exists().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val resource = casProperties.getAuthn().getPac4j().getCore().getGroovyProviderPostProcessor().getLocation();
                    return new DelegatedClientIdentityProviderConfigurationGroovyPostProcessor(new WatchableGroovyScriptResource(resource));
                })
                .otherwise(DelegatedClientIdentityProviderConfigurationPostProcessor::noOp)
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
        public DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final ObjectProvider<DelegatedClientAuthenticationConfigurationContext> configurationContext) {
            return new DefaultDelegatedClientIdentityProviderConfigurationProducer(configurationContext);
        }

        @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderRedirectionStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedAuthenticationCookieGenerator") final CasCookieBuilder delegatedAuthenticationCookieGenerator,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            val chain = new ChainingDelegatedClientIdentityProviderRedirectionStrategy();
            val strategy = casProperties.getAuthn().getPac4j().getCore().getGroovyRedirectionStrategy();
            FunctionUtils.doIfNotNull(strategy.getLocation(),
                resource -> chain.addStrategy(new GroovyDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
                    new WatchableGroovyScriptResource(resource), applicationContext)));
            chain.addStrategy(new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
                delegatedAuthenticationCookieGenerator, casProperties, applicationContext));
            return chain;
        }

        @ConditionalOnMissingBean(name = "delegatedAuthenticationCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder delegatedAuthenticationCookieGenerator(final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getPac4j().getCookie();
            return new DelegatedAuthenticationCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
        }

        @ConditionalOnMissingBean(name = "groovyDelegatedClientAuthenticationRequestCustomizer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientAuthenticationRequestCustomizer groovyDelegatedClientAuthenticationRequestCustomizer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DelegatedClientAuthenticationRequestCustomizer.class)
                .when(BeanCondition.on("cas.authn.pac4j.core.groovy-authentication-request-customizer.location").exists()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val groovy = casProperties.getAuthn().getPac4j().getCore().getGroovyAuthenticationRequestCustomizer();
                    val script = new WatchableGroovyScriptResource(groovy.getLocation());
                    return new GroovyDelegatedClientAuthenticationRequestCustomizer(script, applicationContext);
                })
                .otherwiseProxy()
                .get();
        }


    }

    @Configuration(value = "DelegatedAuthenticationWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowActionsConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION_FINALIZE)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientCredentialSelectionFinalizeAction(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext context,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedClientAuthenticationCredentialSelectionFinalizeAction(context))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION_FINALIZE)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientCredentialSelectionAction(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext context,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedClientAuthenticationCredentialSelectionAction(context))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_FAILURE)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationFailureAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME) final DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedClientAuthenticationFailureAction(delegatedClientAuthenticationFailureEvaluator))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_FAILURE)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationRedirectToClientAction(
            @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME) final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedClientAuthenticationRedirectAction(
                    delegatedClientAuthenticationConfigurationContext, delegatedClientWebflowManager))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_STORE_WEBFLOW_STATE)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationStoreWebflowAction(
            @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME) final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedClientAuthenticationStoreWebflowStateAction(
                    delegatedClientAuthenticationConfigurationContext, delegatedClientWebflowManager))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_STORE_WEBFLOW_STATE)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationIdentityProviderLogoutAction(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedAuthenticationIdentityProviderLogoutAction(delegatedClientAuthenticationConfigurationContext))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_FINALIZE_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationIdentityProviderFinalizeLogoutAction(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedAuthenticationIdentityProviderFinalizeLogoutAction(delegatedClientAuthenticationConfigurationContext))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_FINALIZE_LOGOUT)
                .build()
                .get();
        }


        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientLogoutAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("builtClients") final Clients builtClients,
            @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore) {
            return BeanSupplier.of(Action.class)
                .when(BeanCondition.on("cas.slo.disabled").isFalse().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> WebflowActionBeanSupplier.builder()
                    .withApplicationContext(applicationContext)
                    .withProperties(casProperties)
                    .withAction(() -> new DelegatedAuthenticationClientLogoutAction(builtClients, delegatedClientDistributedSessionStore))
                    .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
                    .build()
                    .get())
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientFinishLogoutAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("builtClients") final Clients builtClients,
            @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedAuthenticationClientFinishLogoutAction(builtClients, delegatedClientDistributedSessionStore))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientRetryAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
            @Qualifier("builtClients") final Clients builtClients) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedAuthenticationClientRetryAction(builtClients, delegatedClientIdentityProviderConfigurationProducer))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
                .build()
                .get();
        }


        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationCreateClientsAction(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> {
                    val ssoEval = new DelegatedAuthenticationSingleSignOnEvaluator(delegatedClientAuthenticationConfigurationContext);
                    return new DelegatedAuthenticationGenerateClientsAction(ssoEval);
                })
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
        @Bean
        public Action delegatedAuthenticationAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME) final DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME) final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DelegatedClientAuthenticationAction(delegatedClientAuthenticationConfigurationContext,
                    delegatedClientWebflowManager, delegatedClientAuthenticationFailureEvaluator))
                .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
                .build()
                .get();
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowContextConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
        public DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext(
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS) final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
            @Qualifier("serviceTicketRequestWebflowEventResolver") final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver") final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier("adaptiveAuthenticationPolicy") final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier("builtClients") final Clients builtClients,
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
            @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
            final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor,
            @Qualifier("delegatedClientDistributedSessionCookieGenerator") final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
            @Qualifier(CentralAuthenticationService.BEAN_NAME) final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("pac4jDelegatedClientNameExtractor") final DelegatedClientNameExtractor pac4jDelegatedClientNameExtractor,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME) final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(ArgumentExtractor.BEAN_NAME) final ArgumentExtractor argumentExtractor,
            @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry,
            @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore,
            @Qualifier(TicketFactory.BEAN_NAME) final TicketFactory ticketFactory,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS) final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("delegatedClientIdentityProviderRedirectionStrategy") final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy,
            @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME) final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME) final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier("delegatedAuthenticationCookieGenerator") final CasCookieBuilder delegatedAuthenticationCookieGenerator,
            @Qualifier("delegatedAuthenticationCredentialExtractor") final DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor,
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<List<DelegatedClientAuthenticationRequestCustomizer>> customizersProvider,
            final ObjectProvider<List<DelegatedClientIdentityProviderAuthorizer>> delegatedClientAuthorizers) {

            val customizers = Optional.ofNullable(customizersProvider.getIfAvailable())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .collect(Collectors.toList());

            val authorizers = Optional.ofNullable(delegatedClientAuthorizers.getIfAvailable())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .collect(Collectors.toList());

            return DelegatedClientAuthenticationConfigurationContext.builder()
                .credentialExtractor(delegatedAuthenticationCredentialExtractor)
                .initialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver)
                .serviceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver)
                .adaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy)
                .clients(builtClients)
                .ticketRegistry(ticketRegistry)
                .applicationContext(applicationContext)
                .servicesManager(servicesManager)
                .delegatedAuthenticationPolicyEnforcer(registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer)
                .authenticationSystemSupport(authenticationSystemSupport)
                .casProperties(casProperties)
                .centralAuthenticationService(centralAuthenticationService)
                .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies)
                .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy)
                .sessionStore(delegatedClientDistributedSessionStore)
                .argumentExtractor(argumentExtractor)
                .ticketFactory(ticketFactory)
                .delegatedClientIdentityProvidersProducer(delegatedClientIdentityProviderConfigurationProducer)
                .delegatedClientIdentityProviderConfigurationPostProcessor(delegatedClientIdentityProviderConfigurationPostProcessor)
                .delegatedClientCookieGenerator(delegatedAuthenticationCookieGenerator)
                .delegatedClientDistributedSessionCookieGenerator(delegatedClientDistributedSessionCookieGenerator)
                .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
                .delegatedClientAuthenticationRequestCustomizers(customizers)
                .delegatedClientNameExtractor(pac4jDelegatedClientNameExtractor)
                .delegatedClientIdentityProviderAuthorizers(authorizers)
                .delegatedClientIdentityProviderRedirectionStrategy(delegatedClientIdentityProviderRedirectionStrategy)
                .build();
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowEndpointsConfiguration {
        private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "delegatedClientEndpointConfigurer")
        public ProtocolEndpointWebSecurityConfigurer<Void> delegatedClientEndpointConfigurer() {
            return new ProtocolEndpointWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT, "/"),
                        StringUtils.prependIfMissing(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER, "/"));
                }
            };
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientsEndpoint delegatedClientsEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("pac4jDelegatedClientFactory") final DelegatedClientFactory pac4jDelegatedClientFactory) {
            return new DelegatedClientsEndpoint(casProperties, pac4jDelegatedClientFactory);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DefaultDelegatedAuthenticationNavigationController defaultDelegatedAuthenticationNavigationController(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
            return new DefaultDelegatedAuthenticationNavigationController(delegatedClientAuthenticationConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController(
            @Qualifier("builtClients") final Clients builtClients,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME) final OpenSamlConfigBean configBean) {
            return new DelegatedSaml2ClientMetadataController(builtClients, configBean);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public HandlerAdapter delegatedClientRedirectWebflowHandlerAdapter(
            @Qualifier("delegatedClientRedirectFlowExecutor") final FlowExecutor delegatedClientRedirectFlowExecutor) {
            val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
            handler.setFlowExecutor(delegatedClientRedirectFlowExecutor);
            handler.setFlowUrlHandler(new CasDefaultFlowUrlHandler());
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerMapping delegatedClientRedirectFlowHandlerMapping(
            @Qualifier("delegatedClientRedirectFlowRegistry") final FlowDefinitionRegistry delegatedClientRedirectFlowRegistry) {
            val handler = new CasFlowHandlerMapping();
            handler.setOrder(0);
            handler.setFlowRegistry(delegatedClientRedirectFlowRegistry);
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry delegatedClientRedirectFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
            return builder.build();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FlowExecutor delegatedClientRedirectFlowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedClientRedirectFlowRegistry") final FlowDefinitionRegistry delegatedClientRedirectFlowRegistry,
            @Qualifier("webflowCipherExecutor") final CipherExecutor webflowCipherExecutor) {
            val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                delegatedClientRedirectFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
            return factory.build();
        }
    }
}
