package org.apereo.cas.ticket.serialization;

import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
        try {
            val serializer = ticketSerializationExecutionPlan.getTicketSerializer(ticket);
            if (serializer == null) {
                throw new IllegalArgumentException("Unable to find ticket serializer for " + ticket.getId());
            }
            return serializer.toString(ticket);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public Ticket deserializeTicket(final String ticketContent, final String type) {
        if (StringUtils.isBlank(type)) {
            throw new InvalidTicketException("Invalid ticket type [blank] specified");
        }
        val serializer = ticketSerializationExecutionPlan.getTicketSerializer(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unable to find ticket deserializer for " + type);
        }
        val clazz = Class.forName(type);
        return deserializeTicket(ticketContent, (Class) clazz);
    }

    @Override
    public <T extends Ticket> T deserializeTicket(final String ticketContent, final Class<T> clazz) {
        val serializer = ticketSerializationExecutionPlan.getTicketSerializer(clazz);
        if (serializer == null) {
            throw new IllegalArgumentException("Unable to find ticket deserializer for " + clazz.getSimpleName());
        }
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
