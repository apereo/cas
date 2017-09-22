package org.apereo.cas.ticket;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link BaseTicketSerializers}
 * that attempts to serialize ticket objects.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseTicketSerializers {
    private static final Map<String, Class> TICKET_TYPE_CACHE = new ConcurrentHashMap<>();

    private static final PrettyPrinter MINIMAL_PRETTY_PRINTER = new MinimalPrettyPrinter();

    /**
     * Gets proxy granting ticket serializer.
     *
     * @return the proxy granting ticket serializer
     */
    public static StringSerializer<ProxyGrantingTicket> getProxyGrantingTicketSerializer() {
        return new AbstractJacksonBackedStringSerializer<ProxyGrantingTicket>(MINIMAL_PRETTY_PRINTER) {
            private static final long serialVersionUID = 7089208351327601379L;

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
        return new AbstractJacksonBackedStringSerializer<ProxyTicket>(MINIMAL_PRETTY_PRINTER) {
            private static final long serialVersionUID = -6343596853082798477L;

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
        return new AbstractJacksonBackedStringSerializer<TicketGrantingTicket>(MINIMAL_PRETTY_PRINTER) {
            private static final long serialVersionUID = 1527874389457723545L;

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
        return new AbstractJacksonBackedStringSerializer<ServiceTicket>(MINIMAL_PRETTY_PRINTER) {
            private static final long serialVersionUID = 8959617299162115085L;

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
     * @param ticketContent the ticket id
     * @param type     the type
     * @return the ticket instance.
     */
    public static <T extends Ticket> T deserializeTicket(final String ticketContent, final String type) {
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
            return deserializeTicket(ticketContent, clazz);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Deserialize ticket.
     *
     * @param <T>           the type parameter
     * @param ticketContent the ticket id
     * @param clazz         the clazz
     * @return the ticket instance
     */
    public static <T extends Ticket> T deserializeTicket(final String ticketContent, final Class<T> clazz) {
        Ticket ticket = null;
        if (TicketGrantingTicket.class.isAssignableFrom(clazz)) {
            ticket = getTicketGrantingTicketSerializer().from(ticketContent);
        } else if (ServiceTicket.class.isAssignableFrom(clazz)) {
            ticket = getServiceTicketSerializer().from(ticketContent);
        }
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
