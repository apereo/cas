package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.registry.CompactTicketAuthentication;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link ProxyGrantingTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ProxyGrantingTicketCompactor implements TicketCompactor<ProxyGrantingTicket> {
    private static final int AUTHENTICATION_INDEX = 3;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val proxyGrantingTicket = (ProxyGrantingTicket) ticket;
        fields.add(StringUtils.defaultString(proxyGrantingTicket.getProxiedBy().getShortenedId()));
        CompactTicketAuthentication.compact(fields, proxyGrantingTicket.getAuthentication());
    }

    @Override
    public Class<ProxyGrantingTicket> getTicketType() {
        return ProxyGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_INDEX + CompactTicketAuthentication.FIELD_COUNT);
        val service = Objects.requireNonNull(serviceFactory.createService(structure.get(CompactTicketIndexes.SERVICE)));
        val authentication = CompactTicketAuthentication.expand(principalFactory, structure.ticketElements(), AUTHENTICATION_INDEX);
        val proxyGrantingTicket = new ProxyGrantingTicketImpl(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX,
            service, null, authentication, new FixedInstantExpirationPolicy(structure.expirationTime()));
        proxyGrantingTicket.setTenantId(service.getTenant());
        proxyGrantingTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return proxyGrantingTicket;
    }
}
