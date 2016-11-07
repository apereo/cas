package org.apereo.cas.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.TicketSerializer;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JacksonJSONSerializer implements TicketSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonJSONSerializer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String serialize(final Ticket ticket) {
        try {
            return MAPPER.writeValueAsString(ticket);
        } catch (final JsonProcessingException e) {
            LOGGER.info("Error writing ticket {}: {}", ticket.getId(), e);
            return "";
        }
    }

    @Override
    public TicketGrantingTicket deserializeTGT(final String ticket) {
        try {
            return MAPPER.readValue(ticket, TicketGrantingTicketImpl.class);
        } catch (final IOException e) {
            LOGGER.info("Error reading TGT: ", e);
            return null;
        }
    }

    @Override
    public ServiceTicket deserializeST(final String ticket) {
        try {
            return MAPPER.readValue(ticket, ServiceTicketImpl.class);
        } catch (final IOException e) {
            LOGGER.info("Error reading ST: ", e);
            return null;
        }
    }
}
