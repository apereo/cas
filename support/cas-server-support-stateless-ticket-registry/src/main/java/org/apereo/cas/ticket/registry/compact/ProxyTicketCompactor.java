package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.registry.CompactTicketAuthentication;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link ProxyTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ProxyTicketCompactor implements TicketCompactor<ProxyTicket> {
    private static final int AUTHENTICATION_INDEX = 3;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val proxyTicket = (ProxyTicket) ticket;
        fields.add(StringUtils.defaultString(proxyTicket.getService().getShortenedId()));
        CompactTicketAuthentication.compact(fields, proxyTicket.getAuthentication());
    }

    @Override
    public Class<ProxyTicket> getTicketType() {
        return ProxyTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_INDEX + CompactTicketAuthentication.FIELD_COUNT);
        val service = Objects.requireNonNull(serviceFactory.createService(structure.get(CompactTicketIndexes.SERVICE)));
        val authentication = CompactTicketAuthentication.expand(principalFactory, structure.ticketElements(), AUTHENTICATION_INDEX);
        val expirationPolicy = new FixedInstantExpirationPolicy(structure.expirationTime());
        val creationTime = DateTimeUtils.zonedDateTimeOf(structure.creationTime());

        val proxyGrantingTicket = new ProxyGrantingTicketImpl(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX,
            service, null, authentication, expirationPolicy);
        proxyGrantingTicket.setTenantId(service.getTenant());
        proxyGrantingTicket.setCreationTime(creationTime);

        val proxyTicket = new ProxyTicketImpl(ProxyTicket.PROXY_TICKET_PREFIX, proxyGrantingTicket, service, false, expirationPolicy);
        proxyTicket.setAuthentication(authentication);
        proxyTicket.setTenantId(service.getTenant());
        proxyTicket.setCreationTime(creationTime);
        return proxyTicket;
    }
}
