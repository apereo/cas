package org.apereo.cas;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.services.ServiceContext;
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

    private final ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory;

    private final ServiceMatchingStrategy serviceMatchingStrategy;

    private final ConfigurableApplicationContext applicationContext;
}
