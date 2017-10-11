package org.apereo.cas.support.rest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.rest.BadRequestException;
import org.apereo.cas.support.rest.CredentialFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class TicketGrantingTicketResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketResource.class);
    private static final String DOCTYPE_AND_TITLE = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>";
    private static final String CLOSE_TITLE_AND_OPEN_FORM = "</title></head><body><h1>TGT Created</h1><form action=\"";
    private static final String TGT_CREATED_TITLE_CONTENT = HttpStatus.CREATED.toString() + ' ' + HttpStatus.CREATED.getReasonPhrase();
    private static final String DOCTYPE_AND_OPENING_FORM = DOCTYPE_AND_TITLE + TGT_CREATED_TITLE_CONTENT + CLOSE_TITLE_AND_OPEN_FORM;
    private static final String REST_OF_THE_FORM_AND_CLOSING_TAGS = "\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\"><br><input "
            + "type=\"submit\" value=\"Submit\"></form></body></html>";
    private static final int SUCCESSFUL_TGT_CREATED_INITIAL_LENGTH = DOCTYPE_AND_OPENING_FORM.length() + REST_OF_THE_FORM_AND_CLOSING_TAGS.length();

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServiceFactory serviceFactory;
    private final CredentialFactory credentialFactory;

    private final ObjectWriter jacksonPrettyWriter = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();

    public TicketGrantingTicketResource(final AuthenticationSystemSupport authenticationSystemSupport,
                                        final CredentialFactory credentialFactory,
                                        final CentralAuthenticationService centralAuthenticationService,
                                        final ServiceFactory serviceFactory) {
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.credentialFactory = credentialFactory;
        this.centralAuthenticationService = centralAuthenticationService;
        this.serviceFactory = serviceFactory;
    }

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request     raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     * @throws Exception in case of JSON parsing failure
     */
    @PostMapping(value = "/v1/tickets", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                             final HttpServletRequest request) throws Exception {

        try {
            final TicketGrantingTicket tgtId = createTicketGrantingTicketForRequest(requestBody, request);
            return createResponseEntityForTicket(request, tgtId);
        } catch (final AuthenticationException e) {
            return createResponseEntityForAuthnFailure(e);
        } catch (final BadRequestException e) {
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

    private ResponseEntity<String> createResponseEntityForTicket(final HttpServletRequest request,
                                                                 final TicketGrantingTicket tgtId) throws Exception {
        final URI ticketReference = new URI(request.getRequestURL().toString() + '/' + tgtId.getId());
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ticketReference);
        final String response;
        if (isDefaultContentType(request)) {
            headers.setContentType(MediaType.TEXT_HTML);
            final String tgtUrl = ticketReference.toString();
            response = new StringBuilder(SUCCESSFUL_TGT_CREATED_INITIAL_LENGTH + tgtUrl.length())
                    .append(DOCTYPE_AND_OPENING_FORM)
                    .append(tgtUrl)
                    .append(REST_OF_THE_FORM_AND_CLOSING_TAGS)
                    .toString();
        } else {
            response = tgtId.getId();
        }
        return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
    }

    private boolean isDefaultContentType(final HttpServletRequest request) {
        final String accept = request.getHeader(HttpHeaders.ACCEPT) == null ? null : request.getHeader(HttpHeaders.ACCEPT).trim();
        return StringUtils.isBlank(accept) || accept.startsWith(MediaType.ALL_VALUE) || accept.startsWith(MediaType.TEXT_HTML_VALUE);
    }

    private TicketGrantingTicket createTicketGrantingTicketForRequest(final MultiValueMap<String, String> requestBody,
                                                                      final HttpServletRequest request) {
        final Credential credential = this.credentialFactory.fromRequestBody(requestBody);
        final Service service = this.serviceFactory.createService(request);
        final AuthenticationResult authenticationResult =
                authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
        return centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
    }

    private ResponseEntity<String> createResponseEntityForAuthnFailure(final AuthenticationException e) {
        final List<String> authnExceptions = e.getHandlerErrors().values().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        final Map<String, List<String>> errorsMap = new HashMap<>();
        errorsMap.put("authentication_exceptions", authnExceptions);
        LOGGER.warn("[{}] Caused by: [{}]", e.getMessage(), authnExceptions);
        try {
            return new ResponseEntity<>(this.jacksonPrettyWriter.writeValueAsString(errorsMap), HttpStatus.UNAUTHORIZED);
        } catch (final JsonProcessingException exception) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
