package org.apereo.cas.support.rest.resources;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.security.auth.login.FailedLoginException;
import java.util.List;

/**
 * CAS RESTful resource for vending and deleting TGTs.
 * <ul>
 * <li>{@code POST /v1/tickets}</li>
 * <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("ticketGrantingTicketResource")
@Slf4j
@Tag(name = "CAS REST")
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
     * @param response    the response
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
    @Operation(summary = "Create ticket-granting ticket",
        parameters = @Parameter(name = "requestBody", required = false, description = "Username and password data"))
    public ResponseEntity<String> createTicketGrantingTicket(@RequestBody(required = false) final MultiValueMap<String, String> requestBody,
                                                             final HttpServletRequest request,
                                                             final HttpServletResponse response) {
        try {
            val tgtId = createTicketGrantingTicketForRequest(requestBody, request, response);
            return createResponseEntityForTicket(request, tgtId);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e, request, applicationContext);
        } catch (final BadRestRequestException e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (final Throwable e) {
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
    @Operation(summary = "Delete ticket-granting ticket",
        parameters = @Parameter(name = "tgtId", in = ParameterIn.PATH, required = true, description = "Ticket-granting ticket id"))
    public ResponseEntity<List<SingleLogoutRequestContext>> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId,
                                                                                       final HttpServletRequest request,
                                                                                       final HttpServletResponse response) {
        val requests = singleLogoutRequestExecutor.execute(tgtId, request, response);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    protected ResponseEntity<String> createResponseEntityForTicket(final HttpServletRequest request, final Ticket tgtId) throws Throwable {
        return ticketGrantingTicketResourceEntityResponseFactory.build(tgtId, request);
    }

    protected Ticket createTicketGrantingTicketForRequest(final MultiValueMap<String, String> requestBody,
                                                          final HttpServletRequest request,
                                                          final HttpServletResponse response) throws Throwable {
        val authenticationResult = authenticationService.authenticate(requestBody, request, response);
        val result = authenticationResult.orElseThrow(FailedLoginException::new);
        return centralAuthenticationService.createTicketGrantingTicket(result);
    }
}
