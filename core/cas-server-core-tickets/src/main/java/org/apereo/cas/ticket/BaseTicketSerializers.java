package org.apereo.cas.ticket;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import java.io.StringWriter;
import java.util.Map;

/**
 * This is {@link BaseTicketSerializers}
 * that attempts to serialize ticket objects.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseTicketSerializers {
    private static final Map<String, Class> TICKET_TYPE_CACHE = Maps.newConcurrentMap();

    /**
     * Gets proxy granting ticket serializer.
     *
     * @return the proxy granting ticket serializer
     */
    public static StringSerializer<ProxyGrantingTicket> getProxyGrantingTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<ProxyGrantingTicket>() {
            @Override
            protected Class<ProxyGrantingTicket> getTypeToSerialize() {
                return ProxyGrantingTicket.class;
            }
        };
    }

    /**
     * Gets proxy ticket serializer.
     *
     * @return the proxy ticket serializer
     */
    public static StringSerializer<ProxyTicket> getProxyTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<ProxyTicket>() {
            @Override
            protected Class<ProxyTicket> getTypeToSerialize() {
                return ProxyTicket.class;
            }
        };
    }

    /**
     * Gets ticket granting ticket serializer.
     *
     * @return the ticket granting ticket serializer
     */
    public static StringSerializer<TicketGrantingTicket> getTicketGrantingTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<TicketGrantingTicket>() {
            @Override
            protected Class<TicketGrantingTicket> getTypeToSerialize() {
                return TicketGrantingTicket.class;
            }
        };
    }

    /**
     * Gets service ticket serializer.
     *
     * @return the service ticket serializer
     */
    public static StringSerializer<ServiceTicket> getServiceTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<ServiceTicket>() {
            @Override
            protected Class<ServiceTicket> getTypeToSerialize() {
                return ServiceTicket.class;
            }
        };
    }

    /**
     * Serialize ticket.
     *
     * @param ticket the ticket
     * @return the string
     */
    public static String serializeTicket(final Ticket ticket) {
        final StringWriter writer = new StringWriter();
        if (ticket instanceof TicketGrantingTicket) {
            getTicketGrantingTicketSerializer().to(writer, TicketGrantingTicket.class.cast(ticket));
        } else if (ticket instanceof ServiceTicket) {
            getServiceTicketSerializer().to(writer, ServiceTicket.class.cast(ticket));
        }

        return writer.toString();
    }

    /**
     * Deserialize ticket.
     *
     * @param <T>      the type parameter
     * @param ticketId the ticket id
     * @param type     the type
     * @return the ticket instance.
     */
    public static <T extends Ticket> T deserializeTicket(final String ticketId, final String type) {
        if (StringUtils.isBlank(type)) {
            throw new InvalidTicketException("Invalid ticket type [blank] specified");
        }
        try {
            final Class<T> clazz;
            if (TICKET_TYPE_CACHE.containsKey(type)) {
                clazz = TICKET_TYPE_CACHE.get(type);
            } else {
                clazz = (Class<T>) Class.forName(type);
                TICKET_TYPE_CACHE.put(type, clazz);
            }
            return deserializeTicket(ticketId, clazz);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Deserialize ticket.
     *
     * @param <T>      the type parameter
     * @param ticketId the ticket id
     * @param clazz    the clazz
     * @return the ticket instance
     */
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
