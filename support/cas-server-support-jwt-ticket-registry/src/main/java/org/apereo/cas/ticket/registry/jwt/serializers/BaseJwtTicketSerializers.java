package org.apereo.cas.ticket.registry.jwt.serializers;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import java.io.StringWriter;

/**
 * This is {@link BaseJwtTicketSerializers}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseJwtTicketSerializers {

    public static StringSerializer<TicketGrantingTicket> getTicketGrantingTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<TicketGrantingTicket>() {
            @Override
            protected Class<TicketGrantingTicket> getTypeToSerialize() {
                return TicketGrantingTicket.class;
            }
        };
    }

    public static StringSerializer<ServiceTicket> getServiceTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<ServiceTicket>() {
            @Override
            protected Class<ServiceTicket> getTypeToSerialize() {
                return ServiceTicket.class;
            }
        };
    }

    public static String serializeTicket(final Ticket ticket) {
        final StringWriter writer = new StringWriter();
        if (ticket instanceof TicketGrantingTicket) {
            getTicketGrantingTicketSerializer().to(writer, TicketGrantingTicket.class.cast(ticket));
        } else if (ticket instanceof ServiceTicket) {
            getServiceTicketSerializer().to(writer, ServiceTicket.class.cast(ticket));
        }

        return writer.toString();
    }

    public static <T extends Ticket> T deserializeTicket(final String ticketId, final String type) {
        if (StringUtils.isBlank(type)) {
            throw new InvalidTicketException("Invalid ticket type [blank] specified");
        }
        try {
            final Class<T> clazz = (Class<T>) Class.forName(type);
            return deserializeTicket(ticketId, clazz);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T extends Ticket> T deserializeTicket(final String ticketId, final Class<T> clazz) {
        Ticket ticket = null;
        if (TicketGrantingTicket.class.isAssignableFrom(clazz)) {
            ticket = getTicketGrantingTicketSerializer().from(ticketId);
        } else if (ServiceTicket.class.isAssignableFrom(clazz)) {
            ticket = getServiceTicketSerializer().from(ticketId);
        }
        if (ticket == null) {
            throw new InvalidTicketException(ticketId);
        }
        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                    + " is of type " + ticket.getClass()
                    + " when we were expecting " + clazz);
        }
        return (T) ticket;
    }

}
