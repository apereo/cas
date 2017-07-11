package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.SurrogateWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;

/**
 * This is {@link SurrogateWebflowEventResolutionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("SurrogateWebflowEventResolutionConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateWebflowEventResolutionConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService surrogateAuthenticationService;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector selector;

    @ConditionalOnMissingBean(name = "surrogateWebflowEventResolver")
    @Bean
    public CasWebflowEventResolver surrogateWebflowEventResolver(@Qualifier("defaultAuthenticationSystemSupport")
                                                                 final AuthenticationSystemSupport authenticationSystemSupport) {
        final CasWebflowEventResolver r = new SurrogateWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                selector, surrogateAuthenticationService);
        this.initialAuthenticationAttemptWebflowEventResolver.addDelegate(r);
        return r;
    }
}
