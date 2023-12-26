package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link org.apereo.cas.ticket.registry.ServiceTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ServiceTicketCompactor implements TicketCompactor<ServiceTicket> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) {
        if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
            builder.append(String.format(",%s", StringUtils.defaultString(sat.getService().getId())));
        } else {
            builder.append(",*");
        }
        if (ticket instanceof final RenewableServiceTicket rst) {
            builder.append(String.format(",%s", BooleanUtils.toString(rst.isFromNewLogin(), "1", "0")));
        } else {
            builder.append(",0");
        }
        return builder.toString();
    }

    @Override
    public Class<ServiceTicket> getTicketType() {
        return ServiceTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val serviceTicketFactory = (ServiceTicketFactory) ticketFactory.getObject().get(getTicketType());
        val ticketElements = List.of(org.springframework.util.StringUtils.commaDelimitedListToStringArray(ticketId));
        val creationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(0)));
        val expirationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(1)));
        val service = serviceFactory.createService(ticketElements.get(2));
        val credentialsProvided = BooleanUtils.toBoolean(ticketElements.get(3));
        val serviceTicket = serviceTicketFactory.create(service, credentialsProvided, getTicketType());
        serviceTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(expirationTimeInSeconds));
        serviceTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(creationTimeInSeconds));
        return serviceTicket;
    }
}
