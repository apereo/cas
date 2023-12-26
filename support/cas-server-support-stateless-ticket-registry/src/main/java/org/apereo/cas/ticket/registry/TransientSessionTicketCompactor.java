package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link org.apereo.cas.ticket.registry.TransientSessionTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TransientSessionTicketCompactor implements TicketCompactor<TransientSessionTicket> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) {
        if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
            builder.append(String.format(",%s", StringUtils.defaultString(sat.getService().getId())));
        } else {
            builder.append(",*");
        }
        return builder.toString();
    }

    @Override
    public Class<TransientSessionTicket> getTicketType() {
        return TransientSessionTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val transientSessionTicketFactory = (TransientSessionTicketFactory) ticketFactory.getObject().get(getTicketType());
        val ticketElements = List.of(org.springframework.util.StringUtils.commaDelimitedListToStringArray(ticketId));

        val creationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(0)));
        val expirationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(1)));
        val service = serviceFactory.createService(ticketElements.get(2));

        val transientTicket = transientSessionTicketFactory.create(service, Map.of());
        transientTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(expirationTimeInSeconds));
        transientTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(creationTimeInSeconds));
        return transientTicket;
    }
}
