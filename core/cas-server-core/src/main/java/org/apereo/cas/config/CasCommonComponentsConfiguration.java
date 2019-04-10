package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class for {@link CasCommonComponents}.
 *
 * @author Dmitriy Kopylenko
 * @since 6.1.0
 */
public class CasCommonComponentsConfiguration {

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("logoutManager")
    private ObjectProvider<LogoutManager> logoutManager;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("principalFactory")
    private ObjectProvider<PrincipalFactory> principalFactory;

    @Autowired
    @Qualifier("protocolTicketCipherExecutor")
    private ObjectProvider<CipherExecutor> protocolTicketCipherExecutor;

    @Bean
    public CasCommonComponents casCommonComponents() {
        return new CasCommonComponents(
                registeredServiceAccessStrategyEnforcer.getIfAvailable(),
                applicationEventPublisher,
                ticketRegistry.getIfAvailable(),
                servicesManager.getIfAvailable(),
                logoutManager.getIfAvailable(),
                ticketFactory.getIfAvailable(),
                principalFactory.getIfAvailable(),
                protocolTicketCipherExecutor.getIfAvailable());
    }
}
