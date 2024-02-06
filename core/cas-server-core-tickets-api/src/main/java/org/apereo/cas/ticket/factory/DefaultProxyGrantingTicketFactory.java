package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.RegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ProxyGrantingTicketIssuerTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
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
    protected final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    @Getter
    protected final ExpirationPolicyBuilder<ProxyGrantingTicket> expirationPolicyBuilder;

    protected final CipherExecutor<String, String> cipherExecutor;

    protected final ServicesManager servicesManager;

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket,
                                                    final Authentication authentication, final Class<T> clazz) throws Throwable {
        val pgtId = produceTicketIdentifier();
        return produceTicket(serviceTicket, authentication, pgtId, clazz);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return ProxyGrantingTicket.class;
    }

    protected <T extends ProxyGrantingTicket> T produceTicket(final ServiceTicket serviceTicket, final Authentication authentication,
                                                              final String pgtId, final Class<T> clazz) {

        val proxyGrantingTicketExpirationPolicy = getProxyGrantingTicketExpirationPolicy(serviceTicket);
        val pgtIssuer = (ProxyGrantingTicketIssuerTicket) serviceTicket;
        val result = produceTicketWithAdequateExpirationPolicy(proxyGrantingTicketExpirationPolicy, pgtIssuer, authentication, pgtId);
        if (result == null || !clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result %s does not match %s ".formatted(result, clazz));
        }
        return (T) result;
    }

    protected RegisteredServiceProxyGrantingTicketExpirationPolicy getProxyGrantingTicketExpirationPolicy(
        final ServiceTicket serviceTicket) {
        val service = servicesManager.findServiceBy(serviceTicket.getService(), CasModelRegisteredService.class);
        if (service != null) {
            return service.getProxyGrantingTicketExpirationPolicy();
        }
        return null;
    }

    protected ProxyGrantingTicket produceTicketWithAdequateExpirationPolicy(
        final RegisteredServiceProxyGrantingTicketExpirationPolicy servicePgtPolicy,
        final ProxyGrantingTicketIssuerTicket serviceTicket,
        final Authentication authentication,
        final String pgtId) {
        if (servicePgtPolicy != null) {
            LOGGER.trace("Overriding proxy-granting ticket policy with the specific policy: [{}]", servicePgtPolicy);
            return serviceTicket.grantProxyGrantingTicket(pgtId, authentication,
                new HardTimeoutExpirationPolicy(servicePgtPolicy.getMaxTimeToLiveInSeconds()));
        }
        LOGGER.trace("Using default ticket-granting ticket policy for proxy-granting ticket");
        return serviceTicket.grantProxyGrantingTicket(pgtId, authentication,
            this.expirationPolicyBuilder.buildTicketExpirationPolicy());

    }

    protected String produceTicketIdentifier() throws Throwable {
        val pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        if (cipherExecutor == null || !cipherExecutor.isEnabled()) {
            return pgtId;
        }
        LOGGER.debug("Attempting to encode proxy-granting ticket [{}]", pgtId);
        val pgtEncoded = this.cipherExecutor.encode(pgtId);
        LOGGER.debug("Encoded proxy-granting ticket id [{}]", pgtEncoded);
        return pgtEncoded;
    }
}
