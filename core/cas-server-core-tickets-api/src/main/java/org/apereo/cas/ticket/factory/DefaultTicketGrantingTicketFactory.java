package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;
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

    @Getter
    protected final ExpirationPolicyBuilder<TicketGrantingTicket> expirationPolicyBuilder;

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
                                                     final Service service, final Class<T> clazz) throws Throwable {
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
        val expirationPolicy = getExpirationPolicyBuilder(service, authentication);
        val result = new TicketGrantingTicketImpl(tgtId, authentication, expirationPolicy.orElseThrow());
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result + "] is of type " + result.getClass() + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    protected Optional<ExpirationPolicy> getExpirationPolicyBuilder(final Service service,
                                                                    final Authentication authentication) {
        val allAttributes = CoreAuthenticationUtils.mergeAttributes(authentication.getAttributes(), authentication.getPrincipal().getAttributes());
        if (allAttributes.containsKey(AuthenticationManager.AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE)) {
            val timeout = CollectionUtils.firstElement(allAttributes.get(AuthenticationManager.AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE))
                .map(value -> NumberUtils.isDigits(value.toString())
                    ? NumberUtils.toLong(value.toString())
                    : Beans.newDuration(value.toString()).toSeconds())
                .orElse(-1L);
            if (timeout >= 0) {
                return Optional.of(new HardTimeoutExpirationPolicy(timeout));
            }
        }

        return Optional.ofNullable(servicesManager.findServiceBy(service))
            .map(RegisteredService::getTicketGrantingTicketExpirationPolicy)
            .flatMap(RegisteredServiceTicketGrantingTicketExpirationPolicy::toExpirationPolicy)
            .or(() -> Optional.of(expirationPolicyBuilder.buildTicketExpirationPolicy()));
    }

    protected String produceTicketIdentifier(final Authentication authentication) throws Throwable {
        var tgtId = ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
        if (cipherExecutor != null && cipherExecutor.isEnabled()) {
            LOGGER.trace("Attempting to encode ticket-granting ticket [{}]", tgtId);
            tgtId = cipherExecutor.encode(tgtId);
            LOGGER.trace("Encoded ticket-granting ticket id [{}]", tgtId);
        }
        return tgtId;
    }
}
