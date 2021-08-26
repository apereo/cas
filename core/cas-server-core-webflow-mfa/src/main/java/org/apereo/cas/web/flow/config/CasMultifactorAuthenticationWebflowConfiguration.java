package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.mfa.trigger.AdaptiveMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.AuthenticationAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.GlobalMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.GroovyScriptMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.HttpRequestMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.PredicatedPrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.PrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.RegisteredServiceMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.RestEndpointMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.ScriptedRegisteredServiceMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.TimedMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
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

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@ConditionalOnWebApplication
public class CasMultifactorAuthenticationWebflowConfiguration {
    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("flowBuilderServices")
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("geoLocationService")
    private ObjectProvider<GeoLocationService> geoLocationService;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private ObjectProvider<MultifactorAuthenticationContextValidator> authenticationContextValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;
    
    @Bean
    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderResolver")
    public MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver() {
        val resolvers = ApplicationContextProvider.getMultifactorAuthenticationPrincipalResolvers();
        return new DefaultMultifactorAuthenticationProviderResolver(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultMultifactorAuthenticationPrincipalResolver")
    public MultifactorAuthenticationPrincipalResolver defaultMultifactorAuthenticationPrincipalResolver() {
        return MultifactorAuthenticationPrincipalResolver.identical();
    }

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            adaptiveMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "adaptiveMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger adaptiveMultifactorAuthenticationTrigger() {
        return new AdaptiveMultifactorAuthenticationTrigger(geoLocationService.getIfAvailable(), casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "timedMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger timedMultifactorAuthenticationTrigger() {
        return new TimedMultifactorAuthenticationTrigger(casProperties, this.applicationContext);
    }

    @ConditionalOnMissingBean(name = "timedAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(), timedMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "principalAttributeMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger principalAttributeMultifactorAuthenticationTrigger() {
        return new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "principalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
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
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext.getObject(),
            predicatedPrincipalAttributeMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "rankedAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver() {
        return new RankedMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            initialAuthenticationAttemptWebflowEventResolver(),
            authenticationContextValidator.getObject(),
            webflowSingleSignOnParticipationStrategy.getObject());
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
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            authenticationAttributeMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderSelector")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector() {
        val mfa = casProperties.getAuthn().getMfa();

        val script = mfa.getCore().getProviderSelectorGroovyScript();
        if (script.getLocation() != null) {
            return new GroovyScriptMultifactorAuthenticationProviderSelector(script.getLocation());
        }
        if (mfa.getCore().isProviderSelectionEnabled()) {
            return new ChainingMultifactorAuthenticationProviderSelector(failureModeEvaluator.getObject());
        }
        return new RankedMultifactorAuthenticationProviderSelector();
    }

    @ConditionalOnMissingBean(name = "initialAuthenticationAttemptWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver() {
        val mfa = casProperties.getAuthn().getMfa();

        val selectiveResolver = mfa.getCore().isProviderSelectionEnabled()
            ? compositeProviderSelectionMultifactorWebflowEventResolver()
            : selectiveAuthenticationProviderWebflowEventResolver();

        val r = new DefaultCasDelegatingWebflowEventResolver(casWebflowConfigurationContext.getObject(), selectiveResolver);
        r.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(timedAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(globalAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(httpRequestAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver());
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
        return new DefaultMultifactorAuthenticationTriggerSelectionStrategy(triggers);
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
            casWebflowConfigurationContext.getObject(),
            restEndpointMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger globalMultifactorAuthenticationTrigger() {
        return new GlobalMultifactorAuthenticationTrigger(casProperties, applicationContext, multifactorAuthenticationProviderSelector());
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            globalMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "groovyScriptMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger groovyScriptMultifactorAuthenticationTrigger() {
        return new GroovyScriptMultifactorAuthenticationTrigger(casProperties, this.applicationContext);
    }

    @ConditionalOnMissingBean(name = "groovyScriptAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            groovyScriptMultifactorAuthenticationTrigger());
    }

    @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver() {
        return new SelectiveMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @ConditionalOnMissingBean(name = "compositeProviderSelectionMultifactorWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver compositeProviderSelectionMultifactorWebflowEventResolver() {
        return new CompositeProviderSelectionMultifactorWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @ConditionalOnMissingBean(name = "httpRequestMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger httpRequestMultifactorAuthenticationTrigger() {
        return new HttpRequestMultifactorAuthenticationTrigger(casProperties, this.applicationContext);
    }

    @ConditionalOnMissingBean(name = "httpRequestAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            httpRequestMultifactorAuthenticationTrigger());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger registeredServicePrincipalAttributeMultifactorAuthenticationTrigger() {
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderResolver(), applicationContext, multifactorAuthenticationProviderSelector());
    }

    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            registeredServicePrincipalAttributeMultifactorAuthenticationTrigger());
    }

    @Bean
    @ConditionalOnMissingBean(name = "scriptedRegisteredServiceMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger scriptedRegisteredServiceMultifactorAuthenticationTrigger() {
        return new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(casProperties, applicationContext);
    }

    @ConditionalOnMissingBean(name = "scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            scriptedRegisteredServiceMultifactorAuthenticationTrigger());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceMultifactorAuthenticationTrigger")
    @RefreshScope
    public MultifactorAuthenticationTrigger registeredServiceMultifactorAuthenticationTrigger() {
        return new RegisteredServiceMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderSelector(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver() {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            registeredServiceMultifactorAuthenticationTrigger());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaAvailableAction")
    public Action mfaAvailableAction() {
        return new MultifactorAuthenticationAvailableAction();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaBypassAction")
    public Action mfaBypassAction() {
        return new MultifactorAuthenticationBypassAction();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaFailureAction")
    public Action mfaFailureAction() {
        return new MultifactorAuthenticationFailureAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeProviderSelectionMultifactorWebflowConfigurer")
    public CasWebflowConfigurer compositeProviderSelectionMultifactorWebflowConfigurer() {
        return new CompositeProviderSelectionMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext,
            casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeProviderSelectionCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer compositeProviderSelectionCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(compositeProviderSelectionMultifactorWebflowConfigurer());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "multifactorProviderSelectedAction")
    public Action multifactorProviderSelectedAction() {
        return new MultifactorProviderSelectedAction();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "prepareMultifactorProviderSelectionAction")
    public Action prepareMultifactorProviderSelectionAction() {
        return new PrepareMultifactorProviderSelectionAction();
    }

}
