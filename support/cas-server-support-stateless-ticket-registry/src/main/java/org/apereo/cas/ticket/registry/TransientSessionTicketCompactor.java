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
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
            builder.append(String.format("%s%s", DELIMITER, StringUtils.defaultString(sat.getService().getShortenedId())));
        } else {
            builder.append("%s*".formatted(DELIMITER));
        }
        return builder.toString();
    }

    @Override
    public Class<TransientSessionTicket> getTicketType() {
        return TransientSessionTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId);
        val transientSessionTicketFactory = (TransientSessionTicketFactory) ticketFactory.getObject().get(getTicketType());
        val service = serviceFactory.createService(structure.ticketElements().get(2));
        val transientTicket = transientSessionTicketFactory.create(service, Map.of());
        transientTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        transientTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return transientTicket;
    }
}
