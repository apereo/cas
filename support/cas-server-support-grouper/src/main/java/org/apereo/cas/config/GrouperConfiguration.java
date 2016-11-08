package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;

/**
 * This is {@link GrouperConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("grouperConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GrouperConfiguration {

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
    @Bean
    @RefreshScope
    public CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver(
            @Qualifier("multifactorAuthenticationProviderSelector") final MultifactorAuthenticationProviderSelector selector) {
        final GrouperMultifactorAuthenticationWebflowEventResolver r = new GrouperMultifactorAuthenticationWebflowEventResolver();
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(selector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
        return r;
    }

}
