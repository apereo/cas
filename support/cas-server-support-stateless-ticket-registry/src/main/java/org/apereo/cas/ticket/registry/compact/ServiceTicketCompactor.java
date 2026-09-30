package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.CompactTicketAuthentication;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link ServiceTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ServiceTicketCompactor implements TicketCompactor<ServiceTicket> {
    private static final int FROM_NEW_LOGIN_INDEX = 3;

    private static final int AUTHENTICATION_INDEX = 4;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val serviceTicket = (ServiceTicket) ticket;
        fields.add(StringUtils.defaultString(serviceTicket.getService().getShortenedId()));
        val fromNewLogin = ticket instanceof final RenewableServiceTicket rst && rst.isFromNewLogin();
        fields.add(BooleanUtils.toString(fromNewLogin, "1", "0"));
        CompactTicketAuthentication.compact(fields, ((AuthenticationAwareTicket) ticket).getAuthentication());
    }

    @Override
    public Class<ServiceTicket> getTicketType() {
        return ServiceTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_INDEX + CompactTicketAuthentication.FIELD_COUNT);
        val service = Objects.requireNonNull(serviceFactory.createService(structure.get(CompactTicketIndexes.SERVICE)));
        val authentication = CompactTicketAuthentication.expand(principalFactory, structure.ticketElements(), AUTHENTICATION_INDEX);
        val fromNewLogin = BooleanUtils.toBoolean(structure.get(FROM_NEW_LOGIN_INDEX));
        val serviceTicket = new ServiceTicketImpl(ServiceTicket.PREFIX, null, service, fromNewLogin,
            new FixedInstantExpirationPolicy(structure.expirationTime())).setAuthentication(authentication);
        serviceTicket.setTenantId(service.getTenant());
        serviceTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return serviceTicket;
    }
}
