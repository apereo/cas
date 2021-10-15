package org.apereo.cas.support.rest.resources;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
@RestController("ticketGrantingTicketResource")
@Slf4j
@RequiredArgsConstructor
public class TicketGrantingTicketResource {
    private final RestAuthenticationService authenticationService;

    private final CentralAuthenticationService centralAuthenticationService;

    private final TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    private final ApplicationContext applicationContext;

    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    /**
     * Reject get response.
     *
     * @return the response entity
     */
    @GetMapping(RestProtocolConstants.ENDPOINT_TICKETS)
    public ResponseEntity<String> rejectGetResponse() {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request     raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     */
    @PostMapping(value = RestProtocolConstants.ENDPOINT_TICKETS,
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_HTML_VALUE,
            MediaType.TEXT_PLAIN_VALUE
        },
        produces = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_HTML_VALUE,
            MediaType.TEXT_PLAIN_VALUE
        })
    public ResponseEntity<String> createTicketGrantingTicket(@RequestBody(required = false) final MultiValueMap<String, String> requestBody,
                                                             final HttpServletRequest request) {
        try {
            val tgtId = createTicketGrantingTicketForRequest(requestBody, request);
            return createResponseEntityForTicket(request, tgtId);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e, request, applicationContext);
        } catch (final BadRestRequestException e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Destroy ticket granting ticket.
     *
     * @param tgtId    ticket granting ticket id URI path param
     * @param request  the request
     * @param response the response
     * @return {@link ResponseEntity} representing RESTful response. Signals {@link HttpStatus#OK} when successful.
     */
    @DeleteMapping(value = RestProtocolConstants.ENDPOINT_TICKETS + "/{tgtId:.+}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SingleLogoutRequestContext>> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId,
                                                                                       final HttpServletRequest request,
                                                                                       final HttpServletResponse response) {
        val requests = singleLogoutRequestExecutor.execute(tgtId, request, response);
        return new ResponseEntity<>(requests, HttpStatus.OK);
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
        return ticketGrantingTicketResourceEntityResponseFactory.build(tgtId, request);
    }

    /**
     * Create ticket granting ticket for request ticket granting ticket.
     *
     * @param requestBody the request body
     * @param request     the request
     * @return the ticket granting ticket
     * @throws Exception the authentication exception
     */
    protected TicketGrantingTicket createTicketGrantingTicketForRequest(final MultiValueMap<String, String> requestBody,
                                                                        final HttpServletRequest request) throws Exception {
        val authenticationResult = authenticationService.authenticate(requestBody, request);
        val result = authenticationResult.orElseThrow(FailedLoginException::new);
        return centralAuthenticationService.createTicketGrantingTicket(result);
    }
}
