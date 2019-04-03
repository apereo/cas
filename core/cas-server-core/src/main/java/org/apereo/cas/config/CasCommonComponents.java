package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;

/**
 * Class encapsulating references to core internal components used throughout CAS server.
 *
 * The purpose is to reduce complexity of big argument lists constructors of CAS classes needing these objects.
 *
 * Typical usage of this class is at Spring constructor injection sites for beans needing these common components which
 * will reduce the size of exposed constructors, etc.
 *
 * @author Dmitriy Kopylenko
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
public class CasCommonComponents {

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final LogoutManager logoutManager;

    private final TicketFactory ticketFactory;

    private final PrincipalFactory principalFactory;

    private final CipherExecutor protocolTicketCipherExecutor;

}
