package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.trigger.AdaptiveMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.AuthenticationAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.GlobalMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.GroovyScriptMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.HttpRequestMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.PredicatedPrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.PrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.RegisteredServiceMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.RestEndpointMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.trigger.TimedMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationAvailableAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationBypassAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationFailureAction;
import org.apereo.cas.web.flow.actions.composite.MultifactorProviderSelectedAction;
import org.apereo.cas.web.flow.actions.composite.PrepareMultifactorProviderSelectionAction;
import org.apereo.cas.web.flow.authentication.ChainingMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.authentication.GroovyScriptMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.configurer.CompositeProviderSelectionMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.CompositeProviderSelectionMultifactorWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.DefaultCasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RankedMultifactorAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.SelectiveMultifactorAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link CasMultifactorAuthenticationWebflowConfiguration}.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Configuration("casMfaWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasMultifactorAuthenticationWebflowConfiguration {
    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("geoLocationService")
    private ObjectProvider<GeoLocationService> geoLocationService;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private ObjectProvider<MultifactorAuthenticationContextValidator> authenticationContextValidator;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Bean
    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderResolver")
    public MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver() {
        return new DefaultMultifactorAuthenticationProviderResolver(multifactorAuthenticationProviderSelector());
    }

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            getWebflowConfigurationContext(),
            adaptiveMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "adaptiveMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger adaptiveMultifactorAuthenticationTrigger() {
        return new AdaptiveMultifactorAuthenticationTrigger(geoLocationService.getIfAvailable(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "timedMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger timedMultifactorAuthenticationTrigger() {
        return new TimedMultifactorAuthenticationTrigger(casProperties);
    }

    @ConditionalOnMissingBean(name = "timedAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext(),
            timedMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "principalAttributeMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger principalAttributeMultifactorAuthenticationTrigger() {
        return new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver());
    }

    @ConditionalOnMissingBean(name = "principalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            getWebflowConfigurationContext(),
            principalAttributeMultifactorAuthenticationTrigger());
    }

    @Bean
    @ConditionalOnMissingBean(name = "predicatedPrincipalAttributeMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger predicatedPrincipalAttributeMultifactorAuthenticationTrigger() {
        return new PredicatedPrincipalAttributeMultifactorAuthenticationTrigger(casProperties, applicationContext);
    }

    @ConditionalOnMissingBean(name = "predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext(),
            predicatedPrincipalAttributeMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "rankedAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver() {
        return new RankedMultifactorAuthenticationProviderWebflowEventResolver(
            getWebflowConfigurationContext(),
            initialAuthenticationAttemptWebflowEventResolver(),
            authenticationContextValidator.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "authenticationAttributeMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger authenticationAttributeMultifactorAuthenticationTrigger() {
        return new AuthenticationAttributeMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "authenticationAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext(),
            authenticationAttributeMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderSelector")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector() {
        val mfa = casProperties.getAuthn().getMfa();

        val script = mfa.getProviderSelectorGroovyScript();
        if (script != null) {
            return new GroovyScriptMultifactorAuthenticationProviderSelector(script);
        }
        if (mfa.isProviderSelectionEnabled()) {
            return new ChainingMultifactorAuthenticationProviderSelector(failureModeEvaluator.getIfAvailable());
        }
        return new RankedMultifactorAuthenticationProviderSelector();
    }

    @ConditionalOnMissingBean(name = "initialAuthenticationAttemptWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver() {
        val mfa = casProperties.getAuthn().getMfa();

        val selectiveResolver = mfa.isProviderSelectionEnabled()
            ? compositeProviderSelectionMultifactorWebflowEventResolver()
            : selectiveAuthenticationProviderWebflowEventResolver();

        val r = new DefaultCasDelegatingWebflowEventResolver(getWebflowConfigurationContext(), selectiveResolver);

        r.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(timedAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(globalAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(httpRequestAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver());
        r.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver());
        return r;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "defaultMultifactorTriggerSelectionStrategy")
    @Autowired
    public MultifactorAuthenticationTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy(final List<MultifactorAuthenticationTrigger> triggers) {
        AnnotationAwareOrderComparator.sortIfNecessary(triggers);
        return new DefaultMultifactorTriggerSelectionStrategy(triggers);
    }

    @Bean
    @ConditionalOnMissingBean(name = "restEndpointMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger restEndpointMultifactorAuthenticationTrigger() {
        return new RestEndpointMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "restEndpointAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            getWebflowConfigurationContext(),
            restEndpointMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger globalMultifactorAuthenticationTrigger() {
        return new GlobalMultifactorAuthenticationTrigger(casProperties, applicationContext);
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            getWebflowConfigurationContext(),
            globalMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "groovyScriptMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger groovyScriptMultifactorAuthenticationTrigger() {
        return new GroovyScriptMultifactorAuthenticationTrigger(casProperties);
    }

    @ConditionalOnMissingBean(name = "groovyScriptAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            getWebflowConfigurationContext(),
            groovyScriptMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver() {
        return new SelectiveMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext());
    }

    @ConditionalOnMissingBean(name = "compositeProviderSelectionMultifactorWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver compositeProviderSelectionMultifactorWebflowEventResolver() {
        return new CompositeProviderSelectionMultifactorWebflowEventResolver(getWebflowConfigurationContext());
    }

    @ConditionalOnMissingBean(name = "httpRequestMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger httpRequestMultifactorAuthenticationTrigger() {
        return new HttpRequestMultifactorAuthenticationTrigger(casProperties);
    }

    @ConditionalOnMissingBean(name = "httpRequestAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext(),
            httpRequestMultifactorAuthenticationTrigger());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger registeredServicePrincipalAttributeMultifactorAuthenticationTrigger() {
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver());
    }

    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext(),
            registeredServicePrincipalAttributeMultifactorAuthenticationTrigger());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger registeredServiceMultifactorAuthenticationTrigger() {
        return new RegisteredServiceMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderSelector());
    }

    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(getWebflowConfigurationContext(),
            registeredServiceMultifactorAuthenticationTrigger());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaAvailableAction")
    public MultifactorAuthenticationAvailableAction mfaAvailableAction() {
        return new MultifactorAuthenticationAvailableAction();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaBypassAction")
    public MultifactorAuthenticationBypassAction mfaBypassAction() {
        return new MultifactorAuthenticationBypassAction();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaFailureAction")
    public MultifactorAuthenticationFailureAction mfaFailureAction() {
        return new MultifactorAuthenticationFailureAction(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeProviderSelectionMultifactorWebflowConfigurer")
    public CasWebflowConfigurer compositeProviderSelectionMultifactorWebflowConfigurer() {
        return new CompositeProviderSelectionMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry.getIfAvailable(),
            applicationContext,
            casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeProviderSelectionCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer compositeProviderSelectionCasWebflowExecutionPlanConfigurer() {
        return new CasWebflowExecutionPlanConfigurer() {
            @Override
            public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
                plan.registerWebflowConfigurer(compositeProviderSelectionMultifactorWebflowConfigurer());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "multifactorProviderSelectedAction")
    public Action multifactorProviderSelectedAction() {
        return new MultifactorProviderSelectedAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "prepareMultifactorProviderSelectionAction")
    public Action prepareMultifactorProviderSelectionAction() {
        return new PrepareMultifactorProviderSelectionAction();
    }

    private CasWebflowEventResolutionConfigurationContext getWebflowConfigurationContext() {
        return CasWebflowEventResolutionConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getIfAvailable())
            .centralAuthenticationService(centralAuthenticationService.getIfAvailable())
            .servicesManager(servicesManager.getIfAvailable())
            .ticketRegistrySupport(ticketRegistrySupport.getIfAvailable())
            .warnCookieGenerator(warnCookieGenerator.getIfAvailable())
            .authenticationRequestServiceSelectionStrategies(authenticationServiceSelectionPlan.getIfAvailable())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getIfAvailable())
            .casProperties(casProperties)
            .eventPublisher(applicationEventPublisher)
            .applicationContext(applicationContext)
            .build();
    }
}
