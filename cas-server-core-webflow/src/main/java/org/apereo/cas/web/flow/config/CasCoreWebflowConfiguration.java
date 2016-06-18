package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.AbstractCasWebflowEventResolver;
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
public class CasCoreWebflowConfiguration {

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

        r.setPrincipalAttributeResolver(
                principalAttributeAuthenticationPolicyWebflowEventResolver());
        r.setPrincipalAttributeResolver(
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

    private void configureResolver(final AbstractCasWebflowEventResolver r) {
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
    }
}
