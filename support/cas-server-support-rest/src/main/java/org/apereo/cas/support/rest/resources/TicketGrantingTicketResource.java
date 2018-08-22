package org.apereo.cas.support.rest.resources;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

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
@RestController("ticketResourceRestController")
@Slf4j
@AllArgsConstructor
public class TicketGrantingTicketResource {

    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final RestHttpRequestCredentialFactory credentialFactory;
    private final CentralAuthenticationService centralAuthenticationService;
    private final ServiceFactory serviceFactory;
    private final TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request     raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     */
    @PostMapping(value = "/v1/tickets", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createTicketGrantingTicket(@RequestBody(required=false) final MultiValueMap<String, String> requestBody,
                                                             final HttpServletRequest request) {
        try {
            final TicketGrantingTicket tgtId = createTicketGrantingTicketForRequest(requestBody, request);
            return createResponseEntityForTicket(request, tgtId);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e);
        } catch (final BadRestRequestException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Destroy ticket granting ticket.
     *
     * @param tgtId ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response. Signals
     * {@link HttpStatus#OK} when successful.
     */
    @DeleteMapping(value = "/v1/tickets/{tgtId:.+}")
    public ResponseEntity<String> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId) {
        this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
        return new ResponseEntity<>(tgtId, HttpStatus.OK);
    }

    /**
     * Create response entity for ticket response entity.
     *
     * @param request the request
     * @param tgtId   the tgt id
     * @return the response entity
     * @throws Exception the exception
     */
    protected ResponseEntity<String> createResponseEntityForTicket(final HttpServletRequest request,
                                                                   final TicketGrantingTicket tgtId) throws Exception {
        return this.ticketGrantingTicketResourceEntityResponseFactory.build(tgtId, request);
    }

    /**
     * Create ticket granting ticket for request ticket granting ticket.
     *
     * @param requestBody the request body
     * @param request     the request
     * @return the ticket granting ticket
     */
    protected TicketGrantingTicket createTicketGrantingTicketForRequest(final MultiValueMap<String, String> requestBody,
                                                                        final HttpServletRequest request) {
        final Collection<Credential> credential = this.credentialFactory.fromRequest(request, requestBody);
        if (credential == null || credential.isEmpty()) {
            throw new BadRestRequestException("No credentials are provided or extracted to authenticate the REST request");
        }
        final Service service = this.serviceFactory.createService(request);
        final AuthenticationResult authenticationResult =
            authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
        return centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
    }
}
