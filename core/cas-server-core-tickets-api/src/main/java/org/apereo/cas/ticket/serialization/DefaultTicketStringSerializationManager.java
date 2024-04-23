package org.apereo.cas.ticket.serialization;

import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import java.util.Objects;

/**
 * This is {@link DefaultTicketStringSerializationManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultTicketStringSerializationManager implements TicketSerializationManager {
    private final TicketSerializationExecutionPlan ticketSerializationExecutionPlan;

    @Override
    public String serializeTicket(final Ticket ticket) {
        val serializer = Objects.requireNonNull(ticketSerializationExecutionPlan.getTicketSerializer(ticket),
            () -> "Unable to find ticket serializer for " + ticket.getId());
        return serializer.toString(ticket);
    }

    @Override
    public Ticket deserializeTicket(final String ticketContent, final String type) {
        if (StringUtils.isBlank(type)) {
            throw new InvalidTicketException("Invalid ticket type [blank] specified");
        }
        val serializer = ticketSerializationExecutionPlan.getTicketSerializer(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unable to find ticket deserializer for " + type);
        }
        return Unchecked.supplier(() -> {
            val clazz = Class.forName(type);
            return deserializeTicket(ticketContent, (Class) clazz);
        }).get();
    }

    @Override
    public <T extends Ticket> T deserializeTicket(final String ticketContent, final Class<T> clazz) {
        val serializer = Objects.requireNonNull(ticketSerializationExecutionPlan.getTicketSerializer(clazz),
            () -> "Unable to find ticket deserializer for " + clazz.getSimpleName());
        LOGGER.trace("Unmarshalling ticket content from [{}]", ticketContent);
        val ticket = serializer.from(ticketContent);
        if (ticket == null) {
            throw new InvalidTicketException(clazz.getName());
        }
        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                                         + " is of type " + ticket.getClass()
                                         + " when we were expecting " + clazz);
        }
        return (T) ticket;
    }
}
