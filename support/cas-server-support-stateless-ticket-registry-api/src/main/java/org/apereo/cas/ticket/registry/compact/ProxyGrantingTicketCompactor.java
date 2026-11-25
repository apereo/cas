package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link ProxyGrantingTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ProxyGrantingTicketCompactor implements TicketCompactor<ProxyGrantingTicket> {
    private static final int AUTHENTICATION_INDEX = 4;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    private final Collection<String> retainedAuthenticationAttributes;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val proxyGrantingTicket = (ProxyGrantingTicket) ticket;
        ServiceTicketCompactor.compactService(fields, proxyGrantingTicket.getProxiedBy());
        CompactTicketAuthentication.compact(fields, proxyGrantingTicket.getAuthentication(), retainedAuthenticationAttributes);
    }

    @Override
    public Class<ProxyGrantingTicket> getTicketType() {
        return ProxyGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_INDEX + CompactTicketAuthentication.FIELD_COUNT);
        val service = ServiceTicketCompactor.expandService(serviceFactory, structure);
        val authentication = CompactTicketAuthentication.expand(principalFactory, structure.ticketElements(), AUTHENTICATION_INDEX);
        val proxyGrantingTicket = newProxyGrantingTicket(ticketFactory.getObject(), service, authentication);
        proxyGrantingTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        proxyGrantingTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return proxyGrantingTicket;
    }

    /**
     * Create a proxy-granting ticket through the service ticket and proxy-granting ticket factories,
     * from a service ticket issued for the proxied service.
     *
     * @param ticketFactory  the ticket factory
     * @param service        the proxied service
     * @param authentication the authentication
     * @return the proxy-granting ticket
     * @throws Throwable the throwable
     */
    static ProxyGrantingTicket newProxyGrantingTicket(final TicketFactory ticketFactory, final Service service,
                                                      final Authentication authentication)
            throws Throwable {
        val serviceTicket = ServiceTicketCompactor.newServiceTicket(ticketFactory, service, authentication, false);
        val factory = (ProxyGrantingTicketFactory<?>) ticketFactory.get(ProxyGrantingTicket.class);
        return factory.create(serviceTicket, authentication);
    }
}
