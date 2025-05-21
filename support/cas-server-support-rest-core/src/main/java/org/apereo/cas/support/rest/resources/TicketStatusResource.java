package org.apereo.cas.support.rest.resources;

import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.LoggingUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * CAS RESTful resource validating TGTs.
 * <ul>
 * <li>{@code GET /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("ticketStatusResourceRestController")
@Slf4j
@RequiredArgsConstructor
public class TicketStatusResource {
    private final TicketRegistry ticketRegistry;

    /**
     * Determine the status of a given ticket id, whether it's valid, exists, expired, etc.
     *
     * @param id ticket id
     * @return {@link ResponseEntity} representing RESTful response
     */
    @GetMapping(RestProtocolConstants.ENDPOINT_TICKETS + "/{id:.+}")
    @Operation(summary = "Get the status of a ticket",
        parameters = @Parameter(name = "id", required = true, in = ParameterIn.PATH, description = "Ticket id"))
    public ResponseEntity<String> getTicketStatus(@PathVariable("id") final String id) {
        try {
            val ticket = ticketRegistry.getTicket(id, Ticket.class);
            return new ResponseEntity<>(ticket.getId(), HttpStatus.OK);
        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>("Ticket could not be found", HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
