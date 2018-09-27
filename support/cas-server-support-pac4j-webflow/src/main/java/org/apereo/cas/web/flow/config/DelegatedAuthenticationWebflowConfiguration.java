package org.apereo.cas.web.flow.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.DelegatedAuthenticationWebApplicationServiceFactory;
import org.apereo.cas.web.DelegatedClientNavigationController;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolver;
import org.apereo.cas.web.flow.DelegatedAuthenticationSAML2ClientLogoutAction;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
import org.apereo.cas.web.saml2.Saml2ClientMetadataController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Collection;

/**
 * This is {@link DelegatedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("delegatedAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class DelegatedAuthenticationWebflowConfiguration implements CasWebflowExecutionPlanConfigurer, ServiceFactoryConfigurer {

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
    private AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer;

    @Autowired
    @Qualifier("builtClients")
    private Clients builtClients;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("pac4jDelegatedSessionCookieManager")
    private DelegatedSessionCookieManager delegatedSessionCookieManager;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("saml2ClientLogoutAction")
    private Action saml2ClientLogoutAction;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Bean
    @ConditionalOnMissingBean(name = "pac4jErrorViewResolver")
    @RefreshScope
    public static ErrorViewResolver pac4jErrorViewResolver() {
        return new DelegatedAuthenticationErrorViewResolver();
    }

    @ConditionalOnMissingBean(name = "saml2ClientLogoutAction")
    @Bean
    @Lazy
    @RefreshScope
    public Action saml2ClientLogoutAction() {
        return new DelegatedAuthenticationSAML2ClientLogoutAction(builtClients);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "clientAction")
    @Bean
    @Lazy
    public Action clientAction() {
        return new DelegatedClientAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver,
            adaptiveAuthenticationPolicy,
            builtClients,
            servicesManager,
            registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
            delegatedClientWebflowManager(),
            delegatedSessionCookieManager,
            authenticationSystemSupport,
            casProperties.getLocale().getParamName(),
            casProperties.getTheme().getParamName(),
            authenticationRequestServiceSelectionStrategies);
    }

    @ConditionalOnMissingBean(name = "delegatedAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer() {
        return new DelegatedAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
            logoutFlowDefinitionRegistry, saml2ClientLogoutAction, applicationContext, casProperties);
    }

    @RefreshScope
    @Bean
    public DelegatedClientWebflowManager delegatedClientWebflowManager() {
        return new DelegatedClientWebflowManager(ticketRegistry,
            ticketFactory,
            casProperties.getTheme().getParamName(),
            casProperties.getLocale().getParamName(),
            authenticationRequestServiceSelectionStrategies,
            argumentExtractor.getIfAvailable()
        );
    }

    @Bean
    public Saml2ClientMetadataController saml2ClientMetadataController() {
        return new Saml2ClientMetadataController(builtClients, configBean);
    }

    @ConditionalOnMissingBean(name = "delegatedClientNavigationController")
    @Bean
    public DelegatedClientNavigationController delegatedClientNavigationController() {
        return new DelegatedClientNavigationController(builtClients, delegatedClientWebflowManager(), delegatedSessionCookieManager);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(delegatedAuthenticationWebflowConfigurer());
    }

    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return CollectionUtils.wrap(new DelegatedAuthenticationWebApplicationServiceFactory(builtClients, delegatedClientWebflowManager()));
    }
}
