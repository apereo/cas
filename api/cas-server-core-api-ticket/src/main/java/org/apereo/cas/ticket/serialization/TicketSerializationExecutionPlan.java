package org.apereo.cas.ticket.serialization;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.serialization.StringSerializer;

/**
 * This is {@link TicketSerializationExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface TicketSerializationExecutionPlan {

    /**
     * Register ticket serializer.
     *
     * @param serializer the serializer
     */
    void registerTicketSerializer(StringSerializer<? extends Ticket> serializer);

    /**
     * Register ticket serializer.
     *
     * @param typeToSerialize the type to serialize
     * @param serializer      the serializer
     */
    void registerTicketSerializer(String typeToSerialize, StringSerializer<? extends Ticket> serializer);

    /**
     * Gets ticket serializer.
     *
     * @param clazz the clazz
     * @return the ticket serializer
     */
    StringSerializer<Ticket> getTicketSerializer(Class<? extends Ticket> clazz);

    /**
     * Gets ticket serializer.
     *
     * @param ticket the ticket
     * @return the ticket serializer
     */
    StringSerializer<Ticket> getTicketSerializer(Ticket ticket);

    /**
     * Gets ticket serializer.
     *
     * @param clazz the clazz
     * @return the ticket serializer
     */
    StringSerializer<Ticket> getTicketSerializer(String clazz);
}
