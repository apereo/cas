package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypePKCETokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20AuthorizationCodeGrantTypePKCETokenRequestValidator extends OAuth20AuthorizationCodeGrantTypeTokenRequestValidator {
    public OAuth20AuthorizationCodeGrantTypePKCETokenRequestValidator(final ServicesManager servicesManager,
                                                                      final TicketRegistry ticketRegistry,
                                                                      final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, ticketRegistry, registeredServiceAccessStrategyEnforcer);
    }
}
