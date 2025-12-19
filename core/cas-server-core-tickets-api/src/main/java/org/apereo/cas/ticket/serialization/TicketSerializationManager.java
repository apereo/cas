package org.apereo.cas.ticket.serialization;

import module java.base;
import org.apereo.cas.ticket.Ticket;

/**
 * This is {@link TicketSerializationManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface TicketSerializationManager {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "ticketSerializationManager";

    /**
     * Serialize ticket.
     *
     * @param ticket the ticket
     * @return the string
     */
    String serializeTicket(Ticket ticket);

    /**
     * Deserialize ticket.
     *
     * @param ticketContent the ticket id
     * @param type          the type
     * @return the ticket instance.
     */
    Ticket deserializeTicket(String ticketContent, String type);

    /**
     * Deserialize ticket.
     *
     * @param <T>           the type parameter
     * @param ticketContent the ticket id
     * @param clazz         the clazz
     * @return the ticket instance
     */
    <T extends Ticket> T deserializeTicket(String ticketContent, Class<T> clazz);
}
