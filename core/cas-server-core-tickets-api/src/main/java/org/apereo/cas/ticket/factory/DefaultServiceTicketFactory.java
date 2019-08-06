package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * The {@link DefaultServiceTicketFactory} is responsible for
 * creating {@link ServiceTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultServiceTicketFactory implements ServiceTicketFactory {
    private final ExpirationPolicyBuilder<ServiceTicket> serviceTicketExpirationPolicy;
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;
    private final boolean trackMostRecentSession;
    private final CipherExecutor<String, String> cipherExecutor;
    private final UniqueTicketIdGenerator defaultServiceTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
    private final ServicesManager servicesManager;

    @Override
    public <T extends Ticket> T create(final TicketGrantingTicket ticketGrantingTicket,
                                       final Service service,
                                       final boolean credentialProvided,
                                       final Class<T> clazz) {
        val ticketId = produceTicketIdentifier(service, ticketGrantingTicket, credentialProvided);
        if (this.cipherExecutor == null) {
            return produceTicket(ticketGrantingTicket, service, credentialProvided, ticketId, clazz);
        }
        LOGGER.trace("Attempting to encode service ticket [{}]", ticketId);
        val encodedId = this.cipherExecutor.encode(ticketId);
        LOGGER.debug("Encoded service ticket id [{}]", encodedId);
        return produceTicket(ticketGrantingTicket, service, credentialProvided, encodedId, clazz);
    }

    /**
     * Produce ticket.
     *
     * @param <T>                  the type parameter
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @param credentialProvided   the credential provided
     * @param ticketId             the ticket id
     * @param clazz                the clazz
     * @return the ticket
     */
    protected <T extends Ticket> T produceTicket(final TicketGrantingTicket ticketGrantingTicket,
                                                 final Service service,
                                                 final boolean credentialProvided,
                                                 final String ticketId,
                                                 final Class<T> clazz) {
        val expirationPolicyToUse = determineExpirationPolicyForService(service);

        val result = ticketGrantingTicket.grantServiceTicket(
            ticketId,
            service,
            expirationPolicyToUse,
            credentialProvided,
            trackMostRecentSession);

        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    private ExpirationPolicy determineExpirationPolicyForService(final Service service) {
        val registeredService = servicesManager.findServiceBy(service);
        if (registeredService != null && registeredService.getServiceTicketExpirationPolicy() != null) {
            val policy = registeredService.getServiceTicketExpirationPolicy();
            val count = policy.getNumberOfUses();
            val ttl = policy.getTimeToLive();
            if (count > 0 && StringUtils.isNotBlank(ttl)) {
                return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
                    count, Beans.newDuration(ttl).getSeconds());
            }
        }
        return this.serviceTicketExpirationPolicy.buildTicketExpirationPolicy();
    }

    /**
     * Produce ticket identifier.
     *
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @param credentialProvided   whether credentials where directly provided
     * @return ticket id
     */
    protected String produceTicketIdentifier(final Service service, final TicketGrantingTicket ticketGrantingTicket,
                                             final boolean credentialProvided) {
        val uniqueTicketIdGenKey = service.getClass().getName();
        var serviceTicketUniqueTicketIdGenerator = (UniqueTicketIdGenerator) null;
        if (this.uniqueTicketIdGeneratorsForService != null && !this.uniqueTicketIdGeneratorsForService.isEmpty()) {
            LOGGER.debug("Looking up service ticket id generator for [{}]", uniqueTicketIdGenKey);
            serviceTicketUniqueTicketIdGenerator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        }
        if (serviceTicketUniqueTicketIdGenerator == null) {
            serviceTicketUniqueTicketIdGenerator = this.defaultServiceTicketIdGenerator;
            LOGGER.debug("Service ticket id generator not found for [{}]. Using the default generator.", uniqueTicketIdGenKey);
        }

        return serviceTicketUniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
