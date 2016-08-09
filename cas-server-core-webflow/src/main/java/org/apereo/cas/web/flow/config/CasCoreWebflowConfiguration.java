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
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.AdaptiveMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.InitialAuthenticationAttemptWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.PrincipalAttributeAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RankedAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RegisteredServiceAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RequestParameterAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.SelectiveAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.ServiceTicketRequestWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;

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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired(required = false)
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector =
            new FirstMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver() {
        final AdaptiveMultifactorAuthenticationWebflowEventResolver r =
                new AdaptiveMultifactorAuthenticationWebflowEventResolver();
        configureResolver(r);
        r.setGeoLocationService(this.geoLocationService);
        return r;
    }
    
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver() {
        final PrincipalAttributeAuthenticationPolicyWebflowEventResolver r =
                new PrincipalAttributeAuthenticationPolicyWebflowEventResolver();
        configureResolver(r);
        return r;
    }

    @Bean
    public MultifactorAuthenticationProviderSelector firstMultifactorAuthenticationProviderSelector() {
        return new FirstMultifactorAuthenticationProviderSelector();
    }

    @Bean
    public CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver() {
        final InitialAuthenticationAttemptWebflowEventResolver r =
                new InitialAuthenticationAttemptWebflowEventResolver();

        r.setAdaptiveAuthenticationResolver(
                adaptiveAuthenticationPolicyWebflowEventResolver());
        
        r.setPrincipalAttributeResolver(
                principalAttributeAuthenticationPolicyWebflowEventResolver());
        
        r.setRegisteredServiceResolver(
                registeredServiceAuthenticationPolicyWebflowEventResolver());
        
        r.setRegisteredServicePrincipalAttributeResolver(
                registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver());
        
        r.setRequestParameterResolver(
                requestParameterAuthenticationPolicyWebflowEventResolver());
        
        r.setSelectiveResolver(
                selectiveAuthenticationProviderWebflowEventResolver());

        configureResolver(r);
        return r;
    }

    @Bean
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver() {
        final ServiceTicketRequestWebflowEventResolver r =
                new ServiceTicketRequestWebflowEventResolver();
        configureResolver(r);
        return r;
    }

    @Bean
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver() {
        final SelectiveAuthenticationProviderWebflowEventResolver r =
                new SelectiveAuthenticationProviderWebflowEventResolver();
        configureResolver(r);
        return r;
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver requestParameterAuthenticationPolicyWebflowEventResolver() {
        final RequestParameterAuthenticationPolicyWebflowEventResolver r =
                new RequestParameterAuthenticationPolicyWebflowEventResolver();
        configureResolver(r);
        return r;
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver() {
        final RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver r =
                new RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver();

        configureResolver(r);
        return r;
    }

    @Bean
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver() {
        final RegisteredServiceAuthenticationPolicyWebflowEventResolver r =
                new RegisteredServiceAuthenticationPolicyWebflowEventResolver();
        configureResolver(r);
        return r;
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver() {
        final RankedAuthenticationProviderWebflowEventResolver r =
                new RankedAuthenticationProviderWebflowEventResolver();
        r.setAuthenticationContextValidator(authenticationContextValidator);
        r.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver());
        configureResolver(r);
        return r;
    }

    @Bean
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        return new WebflowConversationStateCipherExecutor(
                casProperties.getWebflow().getEncryption().getKey(),
                casProperties.getWebflow().getSigning().getKey(),
                casProperties.getWebflow().getAlg(),
                casProperties.getWebflow().getSigning().getKeySize(),
                casProperties.getWebflow().getEncryption().getKeySize());
    }
    
    private void configureResolver(final AbstractCasWebflowEventResolver r) {
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
    }
}
