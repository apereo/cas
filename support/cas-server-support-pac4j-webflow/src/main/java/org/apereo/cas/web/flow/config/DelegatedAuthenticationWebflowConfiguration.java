package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.ChainingDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DefaultDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.GroovyDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.DefaultDelegatedAuthenticationNavigationController;
import org.apereo.cas.web.DefaultDelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.DelegatedAuthenticationCookieGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DefaultDelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientFinishLogoutAction;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientLogoutAction;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientRetryAction;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolver;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationGroovyPostProcessor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationPostProcessor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieUtils;

import lombok.val;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "DelegatedAuthenticationWebflowConfiguration", proxyBeanMethods = false)
public class DelegatedAuthenticationWebflowConfiguration {

    private static DelegatedAuthenticationAccessStrategyHelper getDelegatedAuthenticationAccessStrategyHelper(
        final ServicesManager servicesManager, final AuditableExecution delegatedAuthenticationPolicyAuditableEnforcer) {
        return new DelegatedAuthenticationAccessStrategyHelper(servicesManager, delegatedAuthenticationPolicyAuditableEnforcer);
    }

    @Configuration(value = "DelegatedAuthenticationWebflowErrorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, ResourceProperties.class, WebProperties.class, WebMvcProperties.class})
    public static class DelegatedAuthenticationWebflowErrorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "pac4jErrorViewResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ErrorViewResolver pac4jErrorViewResolver(
            final WebProperties webProperties,
            final ResourceProperties resourceProperties,
            final ConfigurableApplicationContext applicationContext) {
            val resources = (WebProperties.Resources) (webProperties.getResources().hasBeenCustomized()
                ? webProperties.getResources() : resourceProperties);
            return new DelegatedAuthenticationErrorViewResolver(applicationContext, resources);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowExecutionPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "delegatedCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer delegatedCasWebflowExecutionPlanConfigurer(
            @Qualifier("delegatedAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(delegatedAuthenticationWebflowConfigurer);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowPlanConfiguration {
        @ConditionalOnMissingBean(name = "delegatedAuthenticationWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowDefinitionRegistry) {
            return new DelegatedAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                logoutFlowDefinitionRegistry, applicationContext, casProperties);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowManagementConfiguration {
        @ConditionalOnMissingBean(name = DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager(
            @Qualifier("delegatedClientAuthenticationConfigurationContext")
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
            return new DefaultDelegatedClientAuthenticationWebflowManager(delegatedClientAuthenticationConfigurationContext);
        }

        @Bean
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
        @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderConfigurationPostProcessor")
        @RefreshScope
        public DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor(
            final CasConfigurationProperties casProperties) {
            val resource = casProperties.getAuthn().getPac4j().getCore().getGroovyProviderPostProcessor().getLocation();
            if (resource == null) {
                return DelegatedClientIdentityProviderConfigurationPostProcessor.noOp();
            }
            return new DelegatedClientIdentityProviderConfigurationGroovyPostProcessor(
                new WatchableGroovyScriptResource(resource));
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
        @Autowired
        public DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer(
            @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
            final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedClientAuthenticationRequestCustomizers")
            final ObjectProvider<List<DelegatedClientAuthenticationRequestCustomizer>> delegatedClientAuthenticationRequestCustomizers,
            @Qualifier("delegatedClientIdentityProviderRedirectionStrategy")
            final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier("builtClients")
            final Clients builtClients) {

            val customizers = Optional.ofNullable(delegatedClientAuthenticationRequestCustomizers.getIfAvailable()).orElse(new ArrayList<>());
            val helper = getDelegatedAuthenticationAccessStrategyHelper(servicesManager, registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer);
            return new DefaultDelegatedClientIdentityProviderConfigurationProducer(authenticationRequestServiceSelectionStrategies,
                builtClients, helper, casProperties, customizers, delegatedClientIdentityProviderRedirectionStrategy);
        }

        @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderRedirectionStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy(
            final CasConfigurationProperties casProperties,
            @Qualifier("delegatedAuthenticationCookieGenerator")
            final CasCookieBuilder delegatedAuthenticationCookieGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val chain = new ChainingDelegatedClientIdentityProviderRedirectionStrategy();
            val strategy = casProperties.getAuthn().getPac4j().getCore().getGroovyRedirectionStrategy();
            if (strategy.getLocation() != null) {
                chain.addStrategy(new GroovyDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
                    new WatchableGroovyScriptResource(strategy.getLocation())));
            }
            chain.addStrategy(new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
                delegatedAuthenticationCookieGenerator, casProperties));
            return chain;
        }

        @ConditionalOnMissingBean(name = "delegatedAuthenticationCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasCookieBuilder delegatedAuthenticationCookieGenerator(final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getPac4j().getCookie();
            return new DelegatedAuthenticationCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowActionsConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientLogoutAction(
            @Qualifier("builtClients")
            final Clients builtClients,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return new DelegatedAuthenticationClientLogoutAction(builtClients, delegatedClientDistributedSessionStore);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientFinishLogoutAction(
            @Qualifier("builtClients")
            final Clients builtClients,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore) {
            return new DelegatedAuthenticationClientFinishLogoutAction(builtClients, delegatedClientDistributedSessionStore);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action delegatedAuthenticationClientRetryAction(
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
            @Qualifier("builtClients")
            final Clients builtClients) {
            return new DelegatedAuthenticationClientRetryAction(builtClients, delegatedClientIdentityProviderConfigurationProducer);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
        @Bean
        public Action delegatedAuthenticationAction(
            @Qualifier("delegatedClientAuthenticationConfigurationContext")
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
            final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager) {
            return new DelegatedClientAuthenticationAction(delegatedClientAuthenticationConfigurationContext, delegatedClientWebflowManager);
        }
    }

    @Configuration(value = "DelegatedAuthenticationWebflowContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowContextConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
        @Autowired
        public DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext(
            @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
            final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
            @Qualifier("serviceTicketRequestWebflowEventResolver")
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier("adaptiveAuthenticationPolicy")
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("builtClients")
            final Clients builtClients,
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
            @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
            final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor,
            @Qualifier("delegatedClientDistributedSessionCookieGenerator")
            final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("delegatedClientDistributedSessionStore")
            final SessionStore delegatedClientDistributedSessionStore,
            @Qualifier("defaultTicketFactory")
            final TicketFactory ticketFactory,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier("singleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier("delegatedAuthenticationCookieGenerator")
            final CasCookieBuilder delegatedAuthenticationCookieGenerator,
            @Qualifier("delegatedClientAuthenticationRequestCustomizers")
            final ObjectProvider<List<DelegatedClientAuthenticationRequestCustomizer>> delegatedClientAuthenticationRequestCustomizers) {

            val helper = getDelegatedAuthenticationAccessStrategyHelper(servicesManager, registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer);
            val customizers = Optional.ofNullable(delegatedClientAuthenticationRequestCustomizers.getIfAvailable())
                .orElse(new ArrayList<>());

            return DelegatedClientAuthenticationConfigurationContext.builder()
                .initialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver)
                .serviceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver)
                .adaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy)
                .clients(builtClients)
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
                .delegatedAuthenticationAccessStrategyHelper(helper)
                .build();
        }

    }

    @Configuration(value = "DelegatedAuthenticationWebflowEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DelegatedAuthenticationWebflowEndpointsConfiguration {
        @Bean
        public DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController(
            @Qualifier("builtClients")
            final Clients builtClients,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean configBean) {
            return new DelegatedSaml2ClientMetadataController(builtClients, configBean);
        }

        @ConditionalOnMissingBean(name = "delegatedClientNavigationController")
        @Bean
        public DefaultDelegatedAuthenticationNavigationController delegatedClientNavigationController(
            @Qualifier("delegatedClientAuthenticationConfigurationContext")
            final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
            @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
            final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager) {
            return new DefaultDelegatedAuthenticationNavigationController(
                delegatedClientAuthenticationConfigurationContext, delegatedClientWebflowManager);
        }
    }
}
