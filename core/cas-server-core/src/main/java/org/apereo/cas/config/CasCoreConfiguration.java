package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.authentication.DefaultAuthenticationRequestServiceSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@EnableTransactionManagement(proxyTargetClass = true)
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
    public ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory() {
        if (casProperties.getAuthn().getPolicy().isRequiredHandlerAuthenticationPolicyEnabled()) {
            return new RequiredHandlerAuthenticationPolicyFactory();
        }
        return new AcceptAnyAuthenticationPolicyFactory();
    }
    
    @Bean
    public List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies() {
        final List<AuthenticationRequestServiceSelectionStrategy> list = new ArrayList<>();
        list.add(defaultValidationServiceSelectionStrategy());
        return list;
    }

    @Bean
    @Scope(value = "prototype")
    public AuthenticationRequestServiceSelectionStrategy defaultValidationServiceSelectionStrategy() {
        return new DefaultAuthenticationRequestServiceSelectionStrategy();
    }
    
    @Autowired
    @Bean
    public CentralAuthenticationService centralAuthenticationService(
            @Qualifier("authenticationRequestServiceSelectionStrategies") final List<AuthenticationRequestServiceSelectionStrategy> selectionStrategies,
            @Qualifier("principalFactory") final PrincipalFactory principalFactory,
            @Qualifier("protocolTicketCipherExecutor") final CipherExecutor cipherExecutor) {
        return new DefaultCentralAuthenticationService(ticketRegistry, ticketFactory, servicesManager, logoutManager,
                selectionStrategies, authenticationPolicyFactory(), principalFactory, cipherExecutor);
    }
}
