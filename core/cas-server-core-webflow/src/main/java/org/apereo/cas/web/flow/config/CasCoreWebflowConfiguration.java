package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.RedirectToServiceAction;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AdaptiveMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.GlobalMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.InitialAuthenticationAttemptWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.PrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RankedAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RegisteredServiceMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RequestParameterMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RestEndpointMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.impl.SelectiveAuthenticationProviderWebflowEventEventResolver;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreWebflowConfiguration {

    @Autowired
    @Qualifier("webApplicationServiceResponseBuilder")
    private ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder;

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
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;


    @ConditionalOnMissingBean(name = "adaptiveAuthenticationPolicyWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final AdaptiveMultifactorAuthenticationPolicyEventResolver r =
                new AdaptiveMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        r.setGeoLocationService(this.geoLocationService);
        return r;
    }

    @ConditionalOnMissingBean(name = "principalAttributeAuthenticationPolicyWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final PrincipalAttributeMultifactorAuthenticationPolicyEventResolver r =
                new PrincipalAttributeMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "multifactorAuthenticationProviderSelector")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector() {
        return new FirstMultifactorAuthenticationProviderSelector();
    }

    @ConditionalOnMissingBean(name = "initialAuthenticationAttemptWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final InitialAuthenticationAttemptWebflowEventResolver r = new InitialAuthenticationAttemptWebflowEventResolver();
        r.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(globalAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(requestParameterAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver(selector));
        r.setSelectiveResolver(selectiveAuthenticationProviderWebflowEventResolver(selector));
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "restEndpointAuthenticationPolicyWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RestEndpointMultifactorAuthenticationPolicyEventResolver r = new RestEndpointMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final ServiceTicketRequestWebflowEventResolver r = new ServiceTicketRequestWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "globalAuthenticationPolicyWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final GlobalMultifactorAuthenticationPolicyEventResolver r = new GlobalMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "selectiveAuthenticationProviderWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final SelectiveAuthenticationProviderWebflowEventEventResolver r = new SelectiveAuthenticationProviderWebflowEventEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "requestParameterAuthenticationPolicyWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver requestParameterAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RequestParameterMultifactorAuthenticationPolicyEventResolver r = new RequestParameterMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver r =
                new RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RegisteredServiceMultifactorAuthenticationPolicyEventResolver r = new RegisteredServiceMultifactorAuthenticationPolicyEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean(name = "rankedAuthenticationProviderWebflowEventResolver")
    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RankedAuthenticationProviderWebflowEventResolver r = new RankedAuthenticationProviderWebflowEventResolver(authenticationContextValidator,
                initialAuthenticationAttemptWebflowEventResolver(selector));
        configureResolver(r, selector);
        return r;
    }

    @Bean
    @RefreshScope
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        return new WebflowConversationStateCipherExecutor(
                casProperties.getWebflow().getEncryption().getKey(),
                casProperties.getWebflow().getSigning().getKey(),
                casProperties.getWebflow().getAlg(),
                casProperties.getWebflow().getSigning().getKeySize(),
                casProperties.getWebflow().getEncryption().getKeySize());
    }

    @Bean
    public Action clearWebflowCredentialsAction() {
        return new ClearWebflowCredentialAction();
    }

    @Bean
    public Action checkWebAuthenticationRequestAction() {
        final CheckWebAuthenticationRequestAction a = new CheckWebAuthenticationRequestAction();
        a.setContentType(casProperties.getAuthn().getMfa().getContentType());
        return a;
    }

    @Bean
    public Action redirectToServiceAction() {
        return new RedirectToServiceAction(servicesManager, authenticationSystemSupport, ticketRegistrySupport, responseBuilderLocator);
    }

    private void configureResolver(final AbstractCasWebflowEventResolver r, final MultifactorAuthenticationProviderSelector selector) {
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(selector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
        r.setAuthenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies);
    }
}
