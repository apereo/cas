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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Configuration(value = "casMfaWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnWebApplication
public class CasMultifactorAuthenticationWebflowConfiguration {
    @Autowired
    @Qualifier("geoLocationService")
    private ObjectProvider<GeoLocationService> geoLocationService;

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
    @Autowired
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver(
        @Qualifier("adaptiveMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger adaptiveMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            adaptiveMultifactorAuthenticationTrigger);
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
    @Autowired
    public CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver(
        @Qualifier("timedMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger timedMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext, timedMultifactorAuthenticationTrigger);
    }

    @ConditionalOnMissingBean(name = "principalAttributeMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationTrigger principalAttributeMultifactorAuthenticationTrigger(
        @Qualifier("multifactorAuthenticationProviderResolver")
        final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver) {
        return new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderResolver, applicationContext);
    }

    @ConditionalOnMissingBean(name = "principalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver(
        @Qualifier("principalAttributeMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger principalAttributeMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            principalAttributeMultifactorAuthenticationTrigger);
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
    @Autowired
    public CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver(
        @Qualifier("predicatedPrincipalAttributeMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger predicatedPrincipalAttributeMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext,
            predicatedPrincipalAttributeMultifactorAuthenticationTrigger);
    }

    @ConditionalOnMissingBean(name = "rankedAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasDelegatingWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver(
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new RankedMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            initialAuthenticationAttemptWebflowEventResolver,
            authenticationContextValidator.getObject(),
            webflowSingleSignOnParticipationStrategy.getObject());
    }

    @ConditionalOnMissingBean(name = "authenticationAttributeMultifactorAuthenticationTrigger")
    @Bean
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationTrigger authenticationAttributeMultifactorAuthenticationTrigger(
        @Qualifier("multifactorAuthenticationProviderResolver")
        final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver) {
        return new AuthenticationAttributeMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderResolver, applicationContext);
    }

    @ConditionalOnMissingBean(name = "authenticationAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver(
        @Qualifier("authenticationAttributeMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger authenticationAttributeMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            authenticationAttributeMultifactorAuthenticationTrigger);
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
    @Autowired
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver(
        @Qualifier("selectiveAuthenticationProviderWebflowEventResolver")
        final CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
        @Qualifier("adaptiveAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver,
        @Qualifier("timedAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver,
        @Qualifier("globalAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver,
        @Qualifier("httpRequestAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver,
        @Qualifier("restEndpointAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver,
        @Qualifier("groovyScriptAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver,
        @Qualifier("scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver,
        @Qualifier("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier("predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver")
        final CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver,
        @Qualifier("principalAttributeAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier("authenticationAttributeAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier("registeredServiceAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver) {

        val resolver = new DefaultCasDelegatingWebflowEventResolver(casWebflowConfigurationContext,
            selectiveAuthenticationProviderWebflowEventResolver);
        resolver.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(timedAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(globalAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(httpRequestAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver);
        resolver.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver);
        return resolver;
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
    @Autowired
    public MultifactorAuthenticationTrigger restEndpointMultifactorAuthenticationTrigger(
        @Qualifier("multifactorAuthenticationProviderResolver")
        final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver) {
        return new RestEndpointMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver, applicationContext);
    }

    @ConditionalOnMissingBean(name = "restEndpointAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver(
        @Qualifier("restEndpointMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger restEndpointMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            restEndpointMultifactorAuthenticationTrigger);
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationTrigger globalMultifactorAuthenticationTrigger(
        @Qualifier("multifactorAuthenticationProviderSelector")
        final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector) {
        return new GlobalMultifactorAuthenticationTrigger(casProperties, applicationContext, multifactorAuthenticationProviderSelector);
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver(
        @Qualifier("globalMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger globalMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext, globalMultifactorAuthenticationTrigger);
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
    @Autowired
    public CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver(
        @Qualifier("groovyScriptMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger groovyScriptMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            groovyScriptMultifactorAuthenticationTrigger);
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
    @Autowired
    public CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver(
        @Qualifier("httpRequestMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger httpRequestMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            httpRequestMultifactorAuthenticationTrigger);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeMultifactorAuthenticationTrigger")
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationTrigger registeredServicePrincipalAttributeMultifactorAuthenticationTrigger(
        @Qualifier("multifactorAuthenticationProviderSelector")
        final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector,
        @Qualifier("multifactorAuthenticationProviderResolver")
        final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver) {
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderResolver, applicationContext, multifactorAuthenticationProviderSelector);
    }

    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver(
        @Qualifier("registeredServicePrincipalAttributeMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger registeredServicePrincipalAttributeMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            registeredServicePrincipalAttributeMultifactorAuthenticationTrigger);
    }

    @Bean
    @ConditionalOnMissingBean(name = "scriptedRegisteredServiceMultifactorAuthenticationTrigger")
    @RefreshScope
    @Deprecated(since = "6.2.0")
    @SuppressWarnings("InlineMeSuggester")
    public MultifactorAuthenticationTrigger scriptedRegisteredServiceMultifactorAuthenticationTrigger() {
        return new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(casProperties, applicationContext);
    }

    @ConditionalOnMissingBean(name = "scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    @Deprecated(since = "6.2.0")
    @SuppressWarnings("InlineMeSuggester")
    public CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver(
        @Qualifier("scriptedRegisteredServiceMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger scriptedRegisteredServiceMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            scriptedRegisteredServiceMultifactorAuthenticationTrigger);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceMultifactorAuthenticationTrigger")
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationTrigger registeredServiceMultifactorAuthenticationTrigger(
        @Qualifier("multifactorAuthenticationProviderSelector")
        final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector) {
        return new RegisteredServiceMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderSelector, applicationContext);
    }

    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    @Autowired
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver(
        @Qualifier("registeredServiceMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger registeredServiceMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext,
            registeredServiceMultifactorAuthenticationTrigger);
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

    @Configuration(value = "CasCoreMultifactorAuthenticationProviderSelectiveConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.core", name = "provider-selection-enabled", havingValue = "false", matchIfMissing = true)
    public static class CasCoreMultifactorAuthenticationProviderSelectiveConfiguration {
        @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
        @Bean
        @RefreshScope
        @Autowired
        public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new SelectiveMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext);
        }
    }

    @Configuration(value = "CasCoreMultifactorAuthenticationProviderCompositeConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.core", name = "provider-selection-enabled", havingValue = "true")
    public static class CasCoreMultifactorAuthenticationProviderCompositeConfiguration {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Autowired
        private CasConfigurationProperties casProperties;

        @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
        @Bean
        @RefreshScope
        @Autowired
        public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new CompositeProviderSelectionMultifactorWebflowEventResolver(casWebflowConfigurationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "compositeProviderSelectionMultifactorWebflowConfigurer")
        public CasWebflowConfigurer compositeProviderSelectionMultifactorWebflowConfigurer(
            @Qualifier("loginFlowRegistry")
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier("flowBuilderServices")
            final FlowBuilderServices flowBuilderServices) {
            return new CompositeProviderSelectionMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "compositeProviderSelectionCasWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer compositeProviderSelectionCasWebflowExecutionPlanConfigurer(
            @Qualifier("compositeProviderSelectionMultifactorWebflowConfigurer")
            final CasWebflowConfigurer compositeProviderSelectionMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(compositeProviderSelectionMultifactorWebflowConfigurer);
        }
    }
}
