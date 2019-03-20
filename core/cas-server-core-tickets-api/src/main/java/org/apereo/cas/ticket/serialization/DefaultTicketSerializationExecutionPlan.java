package org.apereo.cas.ticket.serialization;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.serialization.serializers.EncodedTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.ProxyGrantingTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.ProxyTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.ServiceTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.TicketGrantingTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.TransientSessionTicketStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultTicketSerializationExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
public class DefaultTicketSerializationExecutionPlan implements TicketSerializationExecutionPlan {
    private final Map<String, StringSerializer<? extends Ticket>> ticketSerializers = new ConcurrentHashMap<>();

    public DefaultTicketSerializationExecutionPlan() {
        registerTicketSerializer(new EncodedTicketStringSerializer());
        registerTicketSerializer(new ProxyGrantingTicketStringSerializer());
        registerTicketSerializer(new ProxyTicketStringSerializer());
        registerTicketSerializer(new ServiceTicketStringSerializer());
        registerTicketSerializer(new TicketGrantingTicketStringSerializer());
        registerTicketSerializer(new TransientSessionTicketStringSerializer());
    }

    @Override
    public void registerTicketSerializer(final StringSerializer<? extends Ticket> serializer) {
        ticketSerializers.put(serializer.getTypeToSerialize().getName(), serializer);
    }

    @Override
    public StringSerializer<Ticket> getTicketSerializer(final Ticket ticket) {
        return getTicketSerializer(ticket.getClass().getName());
    }

    @Override
    public StringSerializer<Ticket> getTicketSerializer(final Class<? extends Ticket> clazz) {
        return getTicketSerializer(clazz.getName());
    }

    @Override
    public StringSerializer<Ticket> getTicketSerializer(final String clazz) {
        return (StringSerializer<Ticket>) ticketSerializers.get(clazz);
    }
}
