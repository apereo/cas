package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.webapp.WebflowProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.web.flow.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.RedirectToServiceAction;
import org.apereo.cas.web.flow.authentication.GroovyScriptMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AdaptiveMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.InitialAuthenticationAttemptWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RankedAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.SelectiveAuthenticationProviderWebflowEventEventResolver;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.GlobalMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.GroovyScriptMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.PrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RegisteredServiceMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RequestParameterMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.mfa.RestEndpointMultifactorAuthenticationPolicyEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreWebflowConfiguration {

    @Autowired(required = false)
    @Qualifier("geoLocationService")
    private GeoLocationService geoLocationService;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("webApplicationResponseBuilderLocator")
    private ResponseBuilderLocator responseBuilderLocator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector selector;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver() {
        return new AdaptiveMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                selector, casProperties, geoLocationService);
    }

    @ConditionalOnMissingBean(name = "principalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new PrincipalAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver() {
        return new PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }


    @ConditionalOnMissingBean(name = "authenticationAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver() {
        return new AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderSelector")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector() {
        final Resource script = casProperties.getAuthn().getMfa().getProviderSelectorGroovyScript();
        if (script != null) {
            return new GroovyScriptMultifactorAuthenticationProviderSelector(script);
        }

        return new RankedMultifactorAuthenticationProviderSelector();
    }

    @ConditionalOnMissingBean(name = "initialAuthenticationAttemptWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver() {
        final InitialAuthenticationAttemptWebflowEventResolver r = new InitialAuthenticationAttemptWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                selector);
        r.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(globalAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(requestParameterAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver());
        r.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver());
        r.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver());
        r.setSelectiveResolver(selectiveAuthenticationProviderWebflowEventResolver());
        return r;
    }

    @ConditionalOnMissingBean(name = "restEndpointAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver() {
        return new RestEndpointMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver() {
        return new ServiceTicketRequestWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver() {
        return new GlobalMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "groovyScriptAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver() {
        return new GroovyScriptMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver() {
        return new SelectiveAuthenticationProviderWebflowEventEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "requestParameterAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver requestParameterAuthenticationPolicyWebflowEventResolver() {
        return new RequestParameterMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector, casProperties);
    }

    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver() {
        return new RegisteredServiceMultifactorAuthenticationPolicyEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies, selector);
    }

    @ConditionalOnMissingBean(name = "rankedAuthenticationProviderWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver() {
        return new RankedAuthenticationProviderWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                selector, authenticationContextValidator,
                initialAuthenticationAttemptWebflowEventResolver());
    }

    @Bean
    @RefreshScope
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        final WebflowProperties webflow = casProperties.getWebflow();
        return new WebflowConversationStateCipherExecutor(
                webflow.getEncryption().getKey(),
                webflow.getSigning().getKey(),
                webflow.getAlg(),
                webflow.getSigning().getKeySize(),
                webflow.getEncryption().getKeySize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "clearWebflowCredentialsAction")
    @RefreshScope
    public Action clearWebflowCredentialsAction() {
        return new ClearWebflowCredentialAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "checkWebAuthenticationRequestAction")
    @RefreshScope
    public Action checkWebAuthenticationRequestAction() {
        return new CheckWebAuthenticationRequestAction(casProperties.getAuthn().getMfa().getContentType());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redirectToServiceAction")
    @RefreshScope
    public Action redirectToServiceAction() {
        return new RedirectToServiceAction(responseBuilderLocator);
    }
}
