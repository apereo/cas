package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.DefaultDelegatedAuthenticationNavigationController;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientLogoutAction;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolver;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationFunction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.saml2.Saml2ClientMetadataController;
import org.apereo.cas.web.support.ArgumentExtractor;

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
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;
import java.util.function.Function;

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
    @Qualifier("shibboleth.OpenSAMLConfig")
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

    @ConditionalOnMissingBean(name = "delegatedAuthenticationClientLogoutAction")
    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientLogoutAction() {
        return new DelegatedAuthenticationClientLogoutAction(builtClients.getObject(),
            delegatedClientDistributedSessionStore.getObject());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    @Bean
    public Action delegatedAuthenticationAction() {
        val configContext = DelegatedClientAuthenticationConfigurationContext.builder()
            .initialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver.getObject())
            .serviceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver.getObject())
            .adaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy.getObject())
            .clients(builtClients.getObject())
            .servicesManager(servicesManager.getObject())
            .delegatedAuthenticationPolicyEnforcer(delegatedAuthenticationPolicyAuditableEnforcer.getObject())
            .delegatedClientWebflowManager(delegatedClientWebflowManager())
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .casProperties(casProperties)
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getObject())
            .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy.getObject())
            .sessionStore(delegatedClientDistributedSessionStore.getObject())
            .argumentExtractors(CollectionUtils.wrap(argumentExtractor.getObject()))
            .delegatedClientIdentityProvidersFunction(delegatedClientIdentityProviderConfigurationFunction())
            .cookieGenerator(delegatedClientDistributedSessionCookieGenerator.getObject())
            .delegatedAuthenticationAccessStrategyHelper(
                new DelegatedAuthenticationAccessStrategyHelper(servicesManager.getObject(),
                    delegatedAuthenticationPolicyAuditableEnforcer.getObject()))
            .build();
        return new DelegatedClientAuthenticationAction(configContext);
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

    @ConditionalOnMissingBean(name = "delegatedClientWebflowManager")
    @RefreshScope
    @Bean
    public DelegatedClientWebflowManager delegatedClientWebflowManager() {
        return new DelegatedClientWebflowManager(centralAuthenticationService.getObject(),
            ticketFactory.getObject(),
            casProperties,
            authenticationRequestServiceSelectionStrategies.getObject(),
            argumentExtractor.getObject()
        );
    }

    @Bean
    public Saml2ClientMetadataController saml2ClientMetadataController() {
        return new Saml2ClientMetadataController(builtClients.getObject(), configBean.getObject());
    }

    @ConditionalOnMissingBean(name = "delegatedClientNavigationController")
    @Bean
    public DefaultDelegatedAuthenticationNavigationController delegatedClientNavigationController() {
        return new DefaultDelegatedAuthenticationNavigationController(builtClients.getObject(),
            delegatedClientWebflowManager(),
            delegatedClientDistributedSessionStore.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "delegatedCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer delegatedCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(delegatedAuthenticationWebflowConfigurer());
    }


    @Bean
    @RefreshScope
    public Function<RequestContext, Set<DelegatedClientIdentityProviderConfiguration>> delegatedClientIdentityProviderConfigurationFunction() {
        val helper = new DelegatedAuthenticationAccessStrategyHelper(this.servicesManager.getObject(),
            delegatedAuthenticationPolicyAuditableEnforcer.getObject());

        return new DelegatedClientIdentityProviderConfigurationFunction(servicesManager.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject(),
            builtClients.getObject(),
            delegatedClientDistributedSessionStore.getObject(),
            helper,
            casProperties);
    }
}
