package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CentralAuthenticationServiceImpl;
import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.DefaultValidationServiceSelectionStrategy;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreConfiguration {

    @Autowired
    @Qualifier("defaultPrincipalFactory")
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

    @Autowired
    @Qualifier("authenticationPolicyFactory")
    private ContextualAuthenticationPolicyFactory serviceContextAuthenticationPolicyFactory =
            new AcceptAnyAuthenticationPolicyFactory();
    
    @Bean
    public List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies() {
        final List list = new ArrayList<>();
        list.add(defaultValidationServiceSelectionStrategy());
        return list;
    }

    @Bean
    @Scope(value = "prototype")
    public ValidationServiceSelectionStrategy defaultValidationServiceSelectionStrategy() {
        return new DefaultValidationServiceSelectionStrategy();
    }
    
    @Autowired
    @Bean
    public CentralAuthenticationService centralAuthenticationService(@Qualifier("validationServiceSelectionStrategies")
                                                                     final List validationServiceSelectionStrategies) {
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
