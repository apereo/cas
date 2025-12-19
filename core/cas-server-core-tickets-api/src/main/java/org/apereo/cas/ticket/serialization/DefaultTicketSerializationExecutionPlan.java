package org.apereo.cas.ticket.serialization;

import module java.base;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.serialization.serializers.EncodedTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.ProxyGrantingTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.ProxyTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.ServiceTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.TicketGrantingTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.TransientSessionTicketStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.Getter;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link DefaultTicketSerializationExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public class DefaultTicketSerializationExecutionPlan implements TicketSerializationExecutionPlan {
    private final Map<String, StringSerializer<? extends Ticket>> ticketSerializers = new ConcurrentHashMap<>();

    public DefaultTicketSerializationExecutionPlan(final ConfigurableApplicationContext applicationContext) {
        registerTicketSerializer(new EncodedTicketStringSerializer(applicationContext));
        registerTicketSerializer(new ProxyGrantingTicketStringSerializer(applicationContext));
        registerTicketSerializer(new ProxyTicketStringSerializer(applicationContext));
        registerTicketSerializer(new ServiceTicketStringSerializer(applicationContext));
        registerTicketSerializer(new TicketGrantingTicketStringSerializer(applicationContext));
        registerTicketSerializer(new TransientSessionTicketStringSerializer(applicationContext));

        registerTicketSerializer(TicketGrantingTicket.class.getName(), new TicketGrantingTicketStringSerializer(applicationContext));
        registerTicketSerializer(ServiceTicket.class.getName(), new ServiceTicketStringSerializer(applicationContext));
        registerTicketSerializer(ProxyTicket.class.getName(), new ProxyTicketStringSerializer(applicationContext));
        registerTicketSerializer(ProxyGrantingTicket.class.getName(), new ProxyGrantingTicketStringSerializer(applicationContext));
        registerTicketSerializer(TransientSessionTicket.class.getName(), new TransientSessionTicketStringSerializer(applicationContext));

        registerTicketSerializer(TicketGrantingTicket.PREFIX, new TicketGrantingTicketStringSerializer(applicationContext));
        registerTicketSerializer(ServiceTicket.PREFIX, new ServiceTicketStringSerializer(applicationContext));
        registerTicketSerializer(ProxyTicket.PROXY_TICKET_PREFIX, new ProxyTicketStringSerializer(applicationContext));
        registerTicketSerializer(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX, new ProxyGrantingTicketStringSerializer(applicationContext));
        registerTicketSerializer(TransientSessionTicket.PREFIX, new TransientSessionTicketStringSerializer(applicationContext));
    }

    @Override
    public void registerTicketSerializer(final StringSerializer<? extends Ticket> serializer) {
        registerTicketSerializer(serializer.getTypeToSerialize().getName(), serializer);
    }

    @Override
    public void registerTicketSerializer(final String typeToSerialize, final StringSerializer<? extends Ticket> serializer) {
        ticketSerializers.put(typeToSerialize, serializer);
    }

    @Override
    public StringSerializer<Ticket> getTicketSerializer(final Class<? extends Ticket> clazz) {
        return getTicketSerializer(clazz.getName());
    }

    @Override
    public StringSerializer<Ticket> getTicketSerializer(final Ticket ticket) {
        return getTicketSerializer(ticket.getClass().getName());
    }

    @Override
    public StringSerializer<Ticket> getTicketSerializer(final String clazz) {
        return (StringSerializer<Ticket>) ticketSerializers.get(clazz);
    }
}
