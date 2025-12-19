package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link DelegatedAuthenticationSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class DelegatedAuthenticationSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {
    public DelegatedAuthenticationSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                                    final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                                                    final TicketRegistrySupport ticketRegistrySupport) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        val registeredService = getRegisteredService(ssoRequest);
        if (registeredService == null) {
            return true;
        }
        val accessStrategy = registeredService.getAccessStrategy();
        if (accessStrategy == null || accessStrategy.getDelegatedAuthenticationPolicy() == null) {
            return true;
        }

        val ticketGrantingTicketId = getTicketGrantingTicketId(ssoRequest);
        if (ticketGrantingTicketId.isEmpty()) {
            return true;
        }

        val authentication = getTicketState(ssoRequest)
            .map(AuthenticationAwareTicket.class::cast)
            .map(AuthenticationAwareTicket::getAuthentication)
            .orElseThrow(() -> new InvalidTicketException(ticketGrantingTicketId.get()));
        val policy = accessStrategy.getDelegatedAuthenticationPolicy();
        val attributes = authentication.getAttributes();
        if (attributes.containsKey(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME)) {
            val clientNameAttr = attributes.get(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME);
            val value = CollectionUtils.firstElement(clientNameAttr);
            if (value.isPresent()) {
                val client = value.get().toString();
                LOGGER.debug("Evaluating delegated access strategy for client [{}] and service [{}]",
                    client, registeredService);
                return policy.isProviderAllowed(client, registeredService);
            }
            return false;
        }
        return !policy.isProviderRequired();
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        val registeredService = getRegisteredService(ssoRequest);
        if (registeredService == null) {
            return false;
        }
        val accessStrategy = registeredService.getAccessStrategy();
        return accessStrategy != null && accessStrategy.getDelegatedAuthenticationPolicy() != null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
