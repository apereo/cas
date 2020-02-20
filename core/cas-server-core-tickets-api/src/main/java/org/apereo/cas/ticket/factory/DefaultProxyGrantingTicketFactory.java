package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The {@link DefaultProxyGrantingTicketFactory} is responsible
 * for creating {@link ProxyGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultProxyGrantingTicketFactory implements ProxyGrantingTicketFactory {
    /**
     * Used to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    protected final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected final ExpirationPolicyBuilder<ProxyGrantingTicket> ticketGrantingTicketExpirationPolicy;

    /**
     * The ticket cipher.
     */
    protected final CipherExecutor<String, String> cipherExecutor;

    /**
     * The service manager.
     */
    protected final ServicesManager servicesManager;

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket,
                                                    final Authentication authentication, final Class<T> clazz) throws AbstractTicketException {
        val pgtId = produceTicketIdentifier();
        return produceTicket(serviceTicket, authentication, pgtId, clazz);
    }

    /**
     * Produce ticket.
     *
     * @param <T>            the type parameter
     * @param serviceTicket  the service ticket
     * @param authentication the authentication
     * @param pgtId          the pgt id
     * @param clazz          the clazz
     * @return the ticket
     */
    protected <T extends ProxyGrantingTicket> T produceTicket(final ServiceTicket serviceTicket, final Authentication authentication,
                                                              final String pgtId, final Class<T> clazz) {

        val service = servicesManager.findServiceBy(serviceTicket.getService());
        val proxyGrantingTicketExpirationPolicy = service.getProxyGrantingTicketExpirationPolicy();

        val result = produceTicketWithAdequateExpirationPolicy(proxyGrantingTicketExpirationPolicy, serviceTicket, authentication, pgtId);

        if (result == null) {
            throw new IllegalArgumentException("Unable to create the proxy-granting ticket object for identifier " + pgtId);
        }
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    /**
     * Produce the ticket with the adequate expiration policy.
     *
     * @param servicePgtPolicy the proxy granting ticket expiration policy
     * @param serviceTicket    the service ticket
     * @param authentication   the authentication
     * @param pgtId            the PGT id
     * @return the ticket
     */
    protected ProxyGrantingTicket produceTicketWithAdequateExpirationPolicy(
            final RegisteredServiceProxyGrantingTicketExpirationPolicy servicePgtPolicy,
            final ServiceTicket serviceTicket,
            final Authentication authentication,
            final String pgtId) {
        if (servicePgtPolicy != null) {
            LOGGER.debug("Overriding PGT policy with the specific policy: {}", servicePgtPolicy);
            return serviceTicket.grantProxyGrantingTicket(pgtId, authentication,
                    new HardTimeoutExpirationPolicy(servicePgtPolicy.getMaxTimeToLiveInSeconds()));
        } else {
            LOGGER.debug("Using default TGT policy for PGT");
            return serviceTicket.grantProxyGrantingTicket(pgtId, authentication,
                    this.ticketGrantingTicketExpirationPolicy.buildTicketExpirationPolicy());
        }
    }

    /**
     * Produce ticket identifier string.
     *
     * @return the ticket
     */
    protected String produceTicketIdentifier() {
        val pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        if (this.cipherExecutor == null) {
            return pgtId;
        }
        LOGGER.debug("Attempting to encode proxy-granting ticket [{}]", pgtId);
        val pgtEncoded = this.cipherExecutor.encode(pgtId);
        LOGGER.debug("Encoded proxy-granting ticket id [{}]", pgtEncoded);
        return pgtEncoded;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
