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
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("delegatedAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationWebflowConfiguration {

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
    private ObjectProvider<AuditableExecution> delegatedAuthenticationPolicyAuditableEnforcer;

    @Autowired
    @Qualifier("builtClients")
    private ObjectProvider<Clients> builtClients;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> configBean;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionCookieGenerator")
    private ObjectProvider<CasCookieBuilder> delegatedClientDistributedSessionCookieGenerator;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private ObjectProvider<SessionStore> delegatedClientDistributedSessionStore;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> logoutFlowDefinitionRegistry;

    @Autowired
    @Qualifier("conventionErrorViewResolver")
    private ObjectProvider<ErrorViewResolver> conventionErrorViewResolver;

    @Bean
    @ConditionalOnMissingBean(name = "pac4jErrorViewResolver")
    @RefreshScope
    public ErrorViewResolver pac4jErrorViewResolver() {
        return new DelegatedAuthenticationErrorViewResolver(conventionErrorViewResolver.getObject());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientLogoutAction() {
        return new DelegatedAuthenticationClientLogoutAction(builtClients.getObject(),
            delegatedClientDistributedSessionStore.getObject());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT)
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientFinishLogoutAction() {
        return new DelegatedAuthenticationClientFinishLogoutAction(builtClients.getObject(),
            delegatedClientDistributedSessionStore.getObject());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientRetryAction() {
        return new DelegatedAuthenticationClientRetryAction(builtClients.getObject(), delegatedClientIdentityProviderConfigurationProducer());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    @Bean
    public Action delegatedAuthenticationAction() {
        return new DelegatedClientAuthenticationAction(delegatedClientAuthenticationConfigurationContext());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
    public DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext() {
        return DelegatedClientAuthenticationConfigurationContext.builder()
            .initialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver.getObject())
            .serviceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver.getObject())
            .adaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy.getObject())
            .clients(builtClients.getObject())
            .servicesManager(servicesManager.getObject())
            .delegatedAuthenticationPolicyEnforcer(delegatedAuthenticationPolicyAuditableEnforcer.getObject())
            .delegatedClientAuthenticationWebflowManager(delegatedClientWebflowManager())
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .casProperties(casProperties)
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getObject())
            .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy.getObject())
            .sessionStore(delegatedClientDistributedSessionStore.getObject())
            .argumentExtractor(argumentExtractor.getObject())
            .ticketFactory(ticketFactory.getObject())
            .delegatedClientIdentityProvidersProducer(delegatedClientIdentityProviderConfigurationProducer())
            .delegatedClientIdentityProviderConfigurationPostProcessor(delegatedClientIdentityProviderConfigurationPostProcessor())
            .delegatedClientCookieGenerator(delegatedAuthenticationCookieGenerator())
            .delegatedClientDistributedSessionCookieGenerator(delegatedClientDistributedSessionCookieGenerator.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .delegatedClientAuthenticationRequestCustomizers(delegatedClientAuthenticationRequestCustomizers())
            .delegatedAuthenticationAccessStrategyHelper(getDelegatedAuthenticationAccessStrategyHelper())
            .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedClientAuthenticationRequestCustomizers")
    @RefreshScope
    public List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers() {
        var customizers = applicationContext.getBeansOfType(DelegatedClientAuthenticationRequestCustomizer.class, false, true).values();
        return new ArrayList<>(customizers);
    }

    @ConditionalOnMissingBean(name = "delegatedAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn({"defaultWebflowConfigurer", "defaultLogoutWebflowConfigurer"})
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer() {
        return new DelegatedAuthenticationWebflowConfigurer(
            flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            logoutFlowDefinitionRegistry.getObject(),
            applicationContext,
            casProperties);
    }

    @ConditionalOnMissingBean(name = DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    @RefreshScope
    @Bean
    public DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager() {
        return new DefaultDelegatedClientAuthenticationWebflowManager(delegatedClientAuthenticationConfigurationContext());
    }

    @Bean
    public DelegatedSaml2ClientMetadataController delegatedSaml2ClientMetadataController() {
        return new DelegatedSaml2ClientMetadataController(builtClients.getObject(), configBean.getObject());
    }

    @ConditionalOnMissingBean(name = "delegatedClientNavigationController")
    @Bean
    public DefaultDelegatedAuthenticationNavigationController delegatedClientNavigationController() {
        return new DefaultDelegatedAuthenticationNavigationController(delegatedClientAuthenticationConfigurationContext());
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer delegatedCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(delegatedAuthenticationWebflowConfigurer());
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
    public DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer() {
        val helper = getDelegatedAuthenticationAccessStrategyHelper();
        return new DefaultDelegatedClientIdentityProviderConfigurationProducer(
            authenticationRequestServiceSelectionStrategies.getObject(),
            builtClients.getObject(),
            helper,
            casProperties,
            delegatedClientAuthenticationRequestCustomizers(),
            delegatedClientIdentityProviderRedirectionStrategy());
    }

    @ConditionalOnMissingBean(name = "delegatedClientIdentityProviderRedirectionStrategy")
    @Bean
    @RefreshScope
    public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy() {
        val chain = new ChainingDelegatedClientIdentityProviderRedirectionStrategy();
        val strategy = casProperties.getAuthn().getPac4j().getCore().getGroovyRedirectionStrategy();
        if (strategy.getLocation() != null) {
            chain.addStrategy(new GroovyDelegatedClientIdentityProviderRedirectionStrategy(servicesManager.getObject(),
                new WatchableGroovyScriptResource(strategy.getLocation())));
        }
        chain.addStrategy(new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager.getObject(),
            delegatedAuthenticationCookieGenerator(), casProperties));
        return chain;
    }

    @ConditionalOnMissingBean(name = "delegatedAuthenticationCookieGenerator")
    @Bean
    @RefreshScope
    public CasCookieBuilder delegatedAuthenticationCookieGenerator() {
        val props = casProperties.getAuthn().getPac4j().getCookie();
        return new DelegatedAuthenticationCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
    }

    private DelegatedAuthenticationAccessStrategyHelper getDelegatedAuthenticationAccessStrategyHelper() {
        return new DelegatedAuthenticationAccessStrategyHelper(servicesManager.getObject(),
            delegatedAuthenticationPolicyAuditableEnforcer.getObject());
    }
}
