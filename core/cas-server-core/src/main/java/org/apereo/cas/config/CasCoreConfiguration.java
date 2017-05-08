package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CentralAuthenticationServiceImpl;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactory;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
public class CasCoreConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
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

    @Bean
    @ConditionalOnMissingBean(name = "authenticationPolicyFactory")
    public ContextualAuthenticationPolicyFactory authenticationPolicyFactory() {
        if (casProperties.getAuthn().getPolicy().isRequiredHandlerAuthenticationPolicyEnabled()) {
            return new RequiredHandlerAuthenticationPolicyFactory();
        }
        return new AcceptAnyAuthenticationPolicyFactory();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "validationServiceSelectionStrategies")
    public List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies() {
        final List list = new ArrayList<>();
        list.add(defaultValidationServiceSelectionStrategy());
        return list;
    }

    @Bean
    @Scope(value = "prototype")
    @ConditionalOnMissingBean(name = "defaultValidationServiceSelectionStrategy")
    public ValidationServiceSelectionStrategy defaultValidationServiceSelectionStrategy() {
        return new DefaultValidationServiceSelectionStrategy();
    }
    
    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "centralAuthenticationService")
    public CentralAuthenticationService centralAuthenticationService(@Qualifier("validationServiceSelectionStrategies")
                                                                     final List validationServiceSelectionStrategies,
                                                                     @Qualifier("principalFactory")
                                                                     final PrincipalFactory principalFactory,
                                                                     @Qualifier("protocolTicketCipherExecutor")
                                                                     final CipherExecutor cipherExecutor) {
        final CentralAuthenticationServiceImpl impl = new CentralAuthenticationServiceImpl();
        impl.setTicketRegistry(this.ticketRegistry);
        impl.setServicesManager(this.servicesManager);
        impl.setLogoutManager(this.logoutManager);
        impl.setTicketFactory(this.ticketFactory);
        impl.setValidationServiceSelectionStrategies(validationServiceSelectionStrategies);
        impl.setServiceContextAuthenticationPolicyFactory(authenticationPolicyFactory());
        impl.setPrincipalFactory(principalFactory);
        impl.setCipherExecutor(cipherExecutor);
        return impl;
    }
}
