package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CentralAuthenticationServiceImpl;
import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreConfiguration")
public class CasCoreConfiguration {

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Resource(name = "validationServiceSelectionStrategies")
    private List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies;

    @Resource(name = "authenticationPolicyFactory")
    private ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory =
            new AcceptAnyAuthenticationPolicyFactory();

    @Bean
    public CentralAuthenticationService centralAuthenticationService() {
        final CentralAuthenticationServiceImpl impl = new CentralAuthenticationServiceImpl();
        impl.setTicketRegistry(this.ticketRegistry);
        impl.setServicesManager(this.servicesManager);
        impl.setLogoutManager(this.logoutManager);
        impl.setTicketFactory(this.ticketFactory);
        impl.setValidationServiceSelectionStrategies(validationServiceSelectionStrategies);
        impl.setServiceContextAuthenticationPolicyFactory(this.serviceContextAuthenticationPolicyFactory);
        impl.setPrincipalFactory(this.principalFactory);
        return impl;
    }
}
