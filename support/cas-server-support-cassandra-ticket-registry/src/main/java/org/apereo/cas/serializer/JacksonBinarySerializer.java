package org.apereo.cas.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apereo.cas.TicketSerializer;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class JacksonBinarySerializer implements TicketSerializer<ByteBuffer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonBinarySerializer.class);
    private ObjectMapper mapper;

    public JacksonBinarySerializer() {
        mapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.findAndRegisterModules();
    }

    @Override
    public ByteBuffer serializeTGT(final Ticket ticket) {
        byte[] serialized;
        try {
            serialized = mapper.writeValueAsBytes(ticket);
        } catch (final JsonProcessingException e) {
            LOGGER.info("Error writing ticket {}: {}", ticket.getId(), e);
            serialized = new byte[]{};
        }
        return ByteBuffer.wrap(serialized);
    }

    @Override
    public ByteBuffer serializeST(final Ticket ticket) {
        byte[] serialized;
        try {
            serialized = mapper.writeValueAsBytes(ticket);
        } catch (final JsonProcessingException e) {
            LOGGER.info("Error writing ticket {}: {}", ticket.getId(), e);
            serialized = new byte[]{};
        }
        return ByteBuffer.wrap(serialized);
    }

    @Override
    public TicketGrantingTicket deserializeTGT(final ByteBuffer ticket) {
        try {
            return mapper.readValue(ticket.array(), TicketGrantingTicketImpl.class);
        } catch (final IOException e) {
            LOGGER.info("Error reading TGT: ", e);
            return null;
        }
    }

    @Override
    public ServiceTicket deserializeST(final ByteBuffer ticket) {
        try {
            return mapper.readValue(ticket.array(), ServiceTicketImpl.class);
        } catch (final IOException e) {
            LOGGER.info("Error reading ST: ", e);
            return null;
        }
    }
}
