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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DelegatedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "delegatedAuthenticationWebflowConfiguration", proxyBeanMethods = false)
public class DelegatedAuthenticationWebflowConfiguration {

    private static DelegatedAuthenticationAccessStrategyHelper getDelegatedAuthenticationAccessStrategyHelper(
        final ServicesManager servicesManager, final AuditableExecution delegatedAuthenticationPolicyAuditableEnforcer) {
        return new DelegatedAuthenticationAccessStrategyHelper(servicesManager, delegatedAuthenticationPolicyAuditableEnforcer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "pac4jErrorViewResolver")
    @RefreshScope
    public ErrorViewResolver pac4jErrorViewResolver(
        @Qualifier("conventionErrorViewResolver")
        final ErrorViewResolver conventionErrorViewResolver) {
        return new DelegatedAuthenticationErrorViewResolver(conventionErrorViewResolver);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientLogoutAction(
        @Qualifier("builtClients")
        final Clients builtClients,
        @Qualifier("delegatedClientDistributedSessionStore")
        final SessionStore delegatedClientDistributedSessionStore) {
        return new DelegatedAuthenticationClientLogoutAction(builtClients, delegatedClientDistributedSessionStore);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT)
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientFinishLogoutAction(
        @Qualifier("builtClients")
        final Clients builtClients,
        @Qualifier("delegatedClientDistributedSessionStore")
        final SessionStore delegatedClientDistributedSessionStore) {
        return new DelegatedAuthenticationClientFinishLogoutAction(builtClients, delegatedClientDistributedSessionStore);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientRetryAction(
        @Qualifier("delegatedClientIdentityProviderConfigurationProducer")
        final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
        @Qualifier("builtClients")
        final Clients builtClients) {
        return new DelegatedAuthenticationClientRetryAction(builtClients, delegatedClientIdentityProviderConfigurationProducer);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    @Bean
    public Action delegatedAuthenticationAction(
        @Qualifier("delegatedClientAuthenticationConfigurationContext")
        final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
        return new DelegatedClientAuthenticationAction(delegatedClientAuthenticationConfigurationContext);
    }

    @Bean
    @RefreshScope
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
        @Qualifier("delegatedClientWebflowManager")
        final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("builtClients")
        final Clients builtClients,
        @Qualifier("delegatedClientIdentityProviderConfigurationProducer")
        final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
        @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
        final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor,
        @Qualifier("delegatedClientDistributedSessionCookieGenerator")
        final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
        @Qualifier("centralAuthenticationService")
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("defaultAuthenticationSystemSupport")
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("argumentExtractor")
        final ArgumentExtractor argumentExtractor,
        @Qualifier("delegatedClientDistributedSessionStore")
        final SessionStore delegatedClientDistributedSessionStore,
        @Qualifier("defaultTicketFactory")
        final TicketFactory ticketFactory,
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier("webflowSingleSignOnParticipationStrategy")
        final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        @Qualifier("delegatedAuthenticationCookieGenerator")
        final CasCookieBuilder delegatedAuthenticationCookieGenerator,
        @Qualifier("delegatedClientAuthenticationRequestCustomizers")
        final List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers) {
        return DelegatedClientAuthenticationConfigurationContext.builder()
            .initialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver)
            .serviceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver)
            .adaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy)
            .clients(builtClients)
            .servicesManager(servicesManager)
            .delegatedAuthenticationPolicyEnforcer(registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer)
            .delegatedClientAuthenticationWebflowManager(delegatedClientWebflowManager)
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
            .delegatedClientAuthenticationRequestCustomizers(delegatedClientAuthenticationRequestCustomizers)
            .delegatedAuthenticationAccessStrategyHelper(getDelegatedAuthenticationAccessStrategyHelper(servicesManager,
                registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer))
            .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedClientAuthenticationRequestCustomizers")
    @RefreshScope
    @Autowired
    public List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers(final ConfigurableApplicationContext applicationContext) {
        var customizers = applicationContext.getBeansOfType(DelegatedClientAuthenticationRequestCustomizer.class, false, true).values();
        return new ArrayList<>(customizers);
    }

    @ConditionalOnMissingBean(name = "delegatedAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn({"defaultWebflowConfigurer", "defaultLogoutWebflowConfigurer"})
    @Autowired
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer(final CasConfigurationProperties casProperties,
                                                                         final ConfigurableApplicationContext applicationContext,
                                                                         @Qualifier("loginFlowDefinitionRegistry")
                                                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                         @Qualifier("flowBuilderServices")
                                                                         final FlowBuilderServices flowBuilderServices,
                                                                         @Qualifier("logoutFlowDefinitionRegistry")
                                                                         final FlowDefinitionRegistry logoutFlowDefinitionRegistry) {
        return new DelegatedAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
            logoutFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    @RefreshScope
    @Bean
    public DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager(
        @Qualifier("delegatedClientAuthenticationConfigurationContext")
        final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
        return new DefaultDelegatedClientAuthenticationWebflowManager(delegatedClientAuthenticationConfigurationContext);
    }

    @Bean
    public DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController(
        @Qualifier("builtClients")
        final Clients builtClients,
        @Qualifier("configBean")
        final OpenSamlConfigBean configBean) {
        return new DelegatedSaml2ClientMetadataController(builtClients, configBean);
    }

    @ConditionalOnMissingBean(name = "delegatedClientNavigationController")
    @Bean
    public DefaultDelegatedAuthenticationNavigationController delegatedClientNavigationController(
        @Qualifier("delegatedClientAuthenticationConfigurationContext")
        final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext) {
        return new DefaultDelegatedAuthenticationNavigationController(delegatedClientAuthenticationConfigurationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer delegatedCasWebflowExecutionPlanConfigurer(
        @Qualifier("delegatedAuthenticationWebflowConfigurer")
        final CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(delegatedAuthenticationWebflowConfigurer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedAuthenticationCasMultifactorWebflowCustomizer")
    public CasMultifactorWebflowCustomizer delegatedAuthenticationCasMultifactorWebflowCustomizer() {
        return () -> List.of(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderConfigurationPostProcessor")
    public DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor() {
        return DelegatedClientIdentityProviderConfigurationPostProcessor.noOp();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderConfigurationProducer")
    @Autowired
    public DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer(
        @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
        final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("delegatedClientAuthenticationRequestCustomizers")
        final List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers,
        @Qualifier("delegatedClientIdentityProviderRedirectionStrategy")
        final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        @Qualifier("builtClients")
        final Clients builtClients) {
        return new DefaultDelegatedClientIdentityProviderConfigurationProducer(authenticationRequestServiceSelectionStrategies,
            builtClients,
            getDelegatedAuthenticationAccessStrategyHelper(servicesManager, registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer),
            casProperties,
            delegatedClientAuthenticationRequestCustomizers,
            delegatedClientIdentityProviderRedirectionStrategy);
    }

    @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderRedirectionStrategy")
    @Bean
    @RefreshScope
    @Autowired
    public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy(
        final CasConfigurationProperties casProperties,
        @Qualifier("delegatedAuthenticationCookieGenerator")
        final CasCookieBuilder delegatedAuthenticationCookieGenerator,
        @Qualifier("servicesManager")
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
    @RefreshScope
    @Autowired
    public CasCookieBuilder delegatedAuthenticationCookieGenerator(final CasConfigurationProperties casProperties) {
        val props = casProperties.getAuthn().getPac4j().getCookie();
        return new DelegatedAuthenticationCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
    }
}
