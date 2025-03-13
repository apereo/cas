package org.apereo.cas;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.lock.LockRepository;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CentralAuthenticationServiceContext}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
public class CentralAuthenticationServiceContext {
    private final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan;

    private final LockRepository lockRepository;

    private final CipherExecutor cipherExecutor;

    private final PrincipalFactory principalFactory;

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final TicketFactory ticketFactory;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final AuthenticationPolicy authenticationPolicy;

    private final ServiceMatchingStrategy serviceMatchingStrategy;

    private final ConfigurableApplicationContext applicationContext;

    private final PrincipalResolver principalResolver;

    private final TenantExtractor tenantExtractor;
}
