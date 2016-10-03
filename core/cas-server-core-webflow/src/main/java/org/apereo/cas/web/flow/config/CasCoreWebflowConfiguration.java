package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.web.flow.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AdaptiveMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.InitialAuthenticationAttemptWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.PrincipalAttributeAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RankedAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RegisteredServiceAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.RequestParameterAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.SelectiveAuthenticationProviderWebflowEventResolver;
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

    @Autowired(required = false)
    @Qualifier("riskAwareAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver;
    
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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final AdaptiveMultifactorAuthenticationWebflowEventResolver r =
                new AdaptiveMultifactorAuthenticationWebflowEventResolver();
        configureResolver(r, selector);
        r.setGeoLocationService(this.geoLocationService);
        return r;
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final PrincipalAttributeAuthenticationPolicyWebflowEventResolver r =
                new PrincipalAttributeAuthenticationPolicyWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @ConditionalOnMissingBean
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector() {
        return new FirstMultifactorAuthenticationProviderSelector();
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final InitialAuthenticationAttemptWebflowEventResolver r = new InitialAuthenticationAttemptWebflowEventResolver();
        r.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver(selector));
        
        if (riskAwareAuthenticationWebflowEventResolver != null) {
            r.addDelegate(riskAwareAuthenticationWebflowEventResolver);
        }
        
        r.addDelegate(requestParameterAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver(selector));
        r.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver(selector));
        r.setSelectiveResolver(selectiveAuthenticationProviderWebflowEventResolver(selector));
        configureResolver(r, selector);
        return r;
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final ServiceTicketRequestWebflowEventResolver r = new ServiceTicketRequestWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final SelectiveAuthenticationProviderWebflowEventResolver r = new SelectiveAuthenticationProviderWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver requestParameterAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RequestParameterAuthenticationPolicyWebflowEventResolver r = new RequestParameterAuthenticationPolicyWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver r =
                new RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RegisteredServiceAuthenticationPolicyWebflowEventResolver r = new RegisteredServiceAuthenticationPolicyWebflowEventResolver();
        configureResolver(r, selector);
        return r;
    }

    @Autowired
    @Bean
    @RefreshScope
    public CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final RankedAuthenticationProviderWebflowEventResolver r =
                new RankedAuthenticationProviderWebflowEventResolver();
        r.setAuthenticationContextValidator(authenticationContextValidator);
        r.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver(selector));
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
    
    private void configureResolver(final AbstractCasWebflowEventResolver r,
                                   final MultifactorAuthenticationProviderSelector selector) {
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(selector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
    }
}
