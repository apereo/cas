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

/**
 * @author David Rodriguez
 *
 * @since 5.1.0
 */
public class JacksonJsonSerializer implements TicketSerializer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonJsonSerializer.class);
    private ObjectMapper mapper;

    public JacksonJsonSerializer() {
        mapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.findAndRegisterModules();
    }

    @Override
    public String serializeTGT(final Ticket ticket) {
        try {
            return mapper.writeValueAsString(ticket);
        } catch (final JsonProcessingException e) {
            LOGGER.info("Error writing ticket {}: {}", ticket.getId(), e);
            return "";
        }
    }

    @Override
    public String serializeST(final Ticket ticket) {
        try {
            return mapper.writeValueAsString(ticket);
        } catch (final JsonProcessingException e) {
            LOGGER.info("Error writing ticket {}: {}", ticket.getId(), e);
            return "";
        }
    }

    @Override
    public TicketGrantingTicket deserializeTGT(final String ticket) {
        try {
            return mapper.readValue(ticket, TicketGrantingTicketImpl.class);
        } catch (final IOException e) {
            LOGGER.info("Error reading TGT: ", e);
            return null;
        }
    }

    @Override
    public ServiceTicket deserializeST(final String ticket) {
        try {
            return mapper.readValue(ticket, ServiceTicketImpl.class);
        } catch (final IOException e) {
            LOGGER.info("Error reading ST: ", e);
            return null;
        }
    }
}
