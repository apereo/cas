package org.apereo.cas.ticket.factory;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;

/**
 * The {@link DefaultProxyTicketFactory} is responsible for
 * creating {@link ProxyTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultProxyTicketFactory implements ProxyTicketFactory {
    private final UniqueTicketIdGenerator defaultTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
    private final ExpirationPolicy proxyTicketExpirationPolicy;
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;
    private final CipherExecutor<String, String> cipherExecutor;
    private final boolean onlyTrackMostRecentSession;
    private final ServicesManager servicesManager;

    @Override
    public <T extends Ticket> T create(final ProxyGrantingTicket proxyGrantingTicket, final Service service,
                                       final Class<T> clazz) {
        val ticketId = produceTicketIdentifier(service);
        return produceTicket(proxyGrantingTicket, service, ticketId, clazz);
    }

    /**
     * Produce ticket.
     *
     * @param <T>                 the type parameter
     * @param proxyGrantingTicket the proxy granting ticket
     * @param service             the service
     * @param ticketId            the ticket id
     * @param clazz               the clazz
     * @return the ticket
     */
    protected <T extends Ticket> T produceTicket(final ProxyGrantingTicket proxyGrantingTicket,
                                                 final Service service, final String ticketId,
                                                 final Class<T> clazz) {
        val expirationPolicyToUse = determineExpirationPolicyForService(service);
        val result = proxyGrantingTicket.grantProxyTicket(
            ticketId,
            service,
            expirationPolicyToUse,
            this.onlyTrackMostRecentSession);

        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    private ExpirationPolicy determineExpirationPolicyForService(final Service service) {
        val registeredService = servicesManager.findServiceBy(service);
        if (registeredService != null) {
            val count = RegisteredServiceProperties.PROXY_TICKET_EXPIRATION_POLICY_NUMBER_OF_USES.getPropertyIntegerValue(registeredService);
            val ttl = RegisteredServiceProperties.PROXY_TICKET_EXPIRATION_POLICY_TIME_TO_LIVE.getPropertyIntegerValue(registeredService);
            if (count > 0 && ttl > 0) {
                return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(count, ttl);
            }
        }
        return this.proxyTicketExpirationPolicy;
    }

    /**
     * Produce ticket identifier.
     *
     * @param service the service
     * @return the ticket id
     */
    protected String produceTicketIdentifier(final Service service) {
        val uniqueTicketIdGenKey = service.getClass().getName();
        LOGGER.debug("Looking up ticket id generator for [{}]", uniqueTicketIdGenKey);
        var generator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        if (generator == null) {
            generator = this.defaultTicketIdGenerator;
            LOGGER.debug("Ticket id generator not found for [{}]. Using the default generator...", uniqueTicketIdGenKey);
        }

        val ticketId = generator.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX);
        if (this.cipherExecutor == null) {
            return ticketId;
        }
        LOGGER.trace("Attempting to encode proxy ticket [{}]", ticketId);
        val encodedId = this.cipherExecutor.encode(ticketId);
        LOGGER.debug("Encoded proxy ticket id [{}]", encodedId);
        return encodedId;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
