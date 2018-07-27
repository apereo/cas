package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator extends OAuth20AuthorizationCodeGrantTypeTokenRequestValidator {
    public OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(final ServicesManager servicesManager,
                                                                                      final TicketRegistry ticketRegistry,
                                                                                      final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, ticketRegistry, registeredServiceAccessStrategyEnforcer);
    }
}
