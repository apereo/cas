package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link ProxyTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ProxyTicketCompactor implements TicketCompactor<ProxyTicket> {
    private static final int AUTHENTICATION_INDEX = 3;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    private final Collection<String> retainedAuthenticationAttributes;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val proxyTicket = (ProxyTicket) ticket;
        fields.add(StringUtils.defaultString(proxyTicket.getService().getShortenedId()));
        CompactTicketAuthentication.compact(fields, proxyTicket.getAuthentication(), retainedAuthenticationAttributes);
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
        val creationTime = DateTimeUtils.zonedDateTimeOf(structure.creationTime());
        val factory = ticketFactory.getObject();

        val proxyGrantingTicket = ProxyGrantingTicketCompactor.newProxyGrantingTicket(factory, service, authentication);
        proxyGrantingTicket.setCreationTime(creationTime);
        proxyGrantingTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));

        val proxyTicketFactory = (ProxyTicketFactory<?>) factory.get(getTicketType());
        val proxyTicket = proxyTicketFactory.create(proxyGrantingTicket, service);
        proxyTicket.setCreationTime(creationTime);
        proxyTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return proxyTicket;
    }
}
