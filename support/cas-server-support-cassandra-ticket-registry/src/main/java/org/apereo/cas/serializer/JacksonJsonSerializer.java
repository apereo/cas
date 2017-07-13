package org.apereo.cas.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.TicketSerializer;
import org.apereo.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
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
    public String serialize(final Ticket ticket) {
        try {
            return mapper.writeValueAsString(ticket);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error writing ticket {}: {}", ticket.getId(), e);
            return StringUtils.EMPTY;
        }
    }

    @Override
    public Ticket deserialize(final String ticket, final Class<? extends Ticket> ticketClass) {
        try {
            return mapper.readValue(ticket, ticketClass);
        } catch (final IOException e) {
            LOGGER.error("Error reading TGT: ", e);
            return null;
        }
    }
}
