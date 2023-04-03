package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link TicketGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketGrantingTicketFactory implements TicketGrantingTicketFactory {
    /**
     * UniqueTicketIdGenerator to generate ids for {@link TicketGrantingTicket}s created.
     */
    protected final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicy;

    /**
     * The ticket cipher, if any.
     */
    protected final CipherExecutor<Serializable, String> cipherExecutor;

    /**
     * The services manager.
     */
    protected final ServicesManager servicesManager;

    @Override
    public <T extends TicketGrantingTicket> T create(final Authentication authentication,
                                                     final Service service,
                                                     final Class<T> clazz) {
        val tgtId = produceTicketIdentifier(authentication);
        return produceTicket(authentication, tgtId, service, clazz);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    protected <T extends TicketGrantingTicket> T produceTicket(final Authentication authentication,
                                                               final String tgtId,
                                                               final Service service,
                                                               final Class<T> clazz) {
        val expirationPolicy = getTicketGrantingTicketExpirationPolicy(service, authentication);
        val result = new TicketGrantingTicketImpl(tgtId, authentication, expirationPolicy.orElseThrow());
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                                         + " is of type " + result.getClass()
                                         + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    protected Optional<ExpirationPolicy> getTicketGrantingTicketExpirationPolicy(final Service service,
                                                                                 final Authentication authentication) {
        return Optional.ofNullable(servicesManager.findServiceBy(service))
            .map(RegisteredService::getTicketGrantingTicketExpirationPolicy)
            .filter(Objects::nonNull)
            .map(policy -> policy.toExpirationPolicy().orElseGet(ticketGrantingTicketExpirationPolicy::buildTicketExpirationPolicy))
            .filter(Objects::nonNull)
            .or(() -> Optional.ofNullable(ticketGrantingTicketExpirationPolicy.buildTicketExpirationPolicy()));
    }

    protected String produceTicketIdentifier(final Authentication authentication) {
        var tgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
        if (this.cipherExecutor != null && this.cipherExecutor.isEnabled()) {
            LOGGER.trace("Attempting to encode ticket-granting ticket [{}]", tgtId);
            tgtId = this.cipherExecutor.encode(tgtId);
            LOGGER.trace("Encoded ticket-granting ticket id [{}]", tgtId);
        }
        return tgtId;
    }
}
