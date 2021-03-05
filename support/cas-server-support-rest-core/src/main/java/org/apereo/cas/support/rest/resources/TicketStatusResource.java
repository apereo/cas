package org.apereo.cas.support.rest.resources;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} implementation of CAS' REST API.
 * <p>
 * This class implements main CAS RESTful resource for vending/deleting TGTs and vending STs:
 * </p>
 * <ul>
 * <li>{@code POST /v1/tickets}</li>
 * <li>{@code POST /v1/tickets/{TGT-id}}</li>
 * <li>{@code GET /v1/tickets/{TGT-id}}</li>
 * <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("ticketStatusResourceRestController")
@Slf4j
@RequiredArgsConstructor
public class TicketStatusResource {
    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Determine the status of a given ticket id, whether it's valid, exists, expired, etc.
     *
     * @param id ticket id
     * @return {@link ResponseEntity} representing RESTful response
     */
    @GetMapping(value = RestProtocolConstants.ENDPOINT_TICKETS + "/{id:.+}")
    public ResponseEntity<String> getTicketStatus(@PathVariable("id") final String id) {
        try {
            val ticket = this.centralAuthenticationService.getTicket(id);
            return new ResponseEntity<>(ticket.getId(), HttpStatus.OK);
        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>("Ticket could not be found", HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
