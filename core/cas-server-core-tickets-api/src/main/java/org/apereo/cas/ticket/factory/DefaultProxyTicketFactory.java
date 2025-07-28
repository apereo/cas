package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
public class DefaultProxyTicketFactory implements ProxyTicketFactory<ProxyTicket> {
    @Getter
    private final UniqueTicketIdGenerator ticketIdGenerator = new HostNameBasedUniqueTicketIdGenerator();

    @Getter
    private final ExpirationPolicyBuilder<ProxyTicket> expirationPolicyBuilder;

    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    private final CipherExecutor<String, String> cipherExecutor;

    private final TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    private final ServicesManager servicesManager;

    @Override
    public ProxyTicket create(final ProxyGrantingTicket proxyGrantingTicket, final Service service) throws Throwable {
        val ticketId = produceTicketIdentifier(service);
        return produceTicket(proxyGrantingTicket, service, ticketId);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return ProxyTicket.class;
    }

    protected ProxyTicket produceTicket(final ProxyGrantingTicket proxyGrantingTicket,
                                        final Service service, final String ticketId) {
        val expirationPolicyToUse = determineExpirationPolicyForService(service);
        return proxyGrantingTicket.grantProxyTicket(
            ticketId,
            service,
            expirationPolicyToUse,
            serviceTicketSessionTrackingPolicy);
    }

    protected String produceTicketIdentifier(final Service service) throws Throwable {
        val uniqueTicketIdGenKey = service.getClass().getName();
        LOGGER.debug("Looking up ticket id generator for [{}]", uniqueTicketIdGenKey);
        var generator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        if (generator == null) {
            generator = this.ticketIdGenerator;
            LOGGER.debug("Ticket id generator not found for [{}]. Using the default generator...", uniqueTicketIdGenKey);
        }

        val ticketId = generator.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX);
        if (cipherExecutor == null || !cipherExecutor.isEnabled()) {
            return ticketId;
        }
        LOGGER.trace("Attempting to encode proxy ticket [{}]", ticketId);
        val encodedId = this.cipherExecutor.encode(ticketId);
        LOGGER.debug("Encoded proxy ticket id [{}]", encodedId);
        return encodedId;
    }

    private ExpirationPolicy determineExpirationPolicyForService(final Service service) {
        val registeredService = servicesManager.findServiceBy(service, CasModelRegisteredService.class);
        if (registeredService != null && registeredService.getProxyTicketExpirationPolicy() != null) {
            val policy = registeredService.getProxyTicketExpirationPolicy();
            val count = policy.getNumberOfUses();
            val ttl = policy.getTimeToLive();
            if (count > 0 && StringUtils.isNotBlank(ttl)) {
                return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(count,
                    Beans.newDuration(ttl).toSeconds());
            }
        }
        return expirationPolicyBuilder.buildTicketExpirationPolicy();
    }
}
