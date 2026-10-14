package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

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

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    private final Collection<String> retainedAuthenticationAttributes;

    @Getter
    private long maximumTicketLength = 256;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val serviceTicket = (ServiceTicket) ticket;
        fields.add(StringUtils.defaultString(serviceTicket.getService().getShortenedId()));
        val fromNewLogin = ticket instanceof final RenewableServiceTicket rst && rst.isFromNewLogin();
        fields.add(BooleanUtils.toString(fromNewLogin, "1", "0"));
        CompactTicketAuthentication.compact(fields, ((AuthenticationAwareTicket) ticket).getAuthentication(), retainedAuthenticationAttributes);
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
        val serviceTicket = newServiceTicket(ticketFactory.getObject(), service, authentication, fromNewLogin);
        serviceTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        serviceTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return serviceTicket;
    }

    /**
     * Create a service ticket through the service ticket factory.
     *
     * @param ticketFactory  the ticket factory
     * @param service        the service
     * @param authentication the authentication
     * @param fromNewLogin   whether the ticket was issued from a new login
     * @return the service ticket
     * @throws Throwable the throwable
     */
    static ServiceTicket newServiceTicket(final TicketFactory ticketFactory, final Service service,
                                          final Authentication authentication, final boolean fromNewLogin)
            throws Throwable {
        val factory = (ServiceTicketFactory) ticketFactory.get(ServiceTicket.class);
        return factory.create(service, authentication, fromNewLogin, ServiceTicket.class);
    }
}
