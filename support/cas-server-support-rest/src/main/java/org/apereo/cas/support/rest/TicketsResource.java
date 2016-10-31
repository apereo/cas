package org.apereo.cas.support.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
public class TicketsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResource.class);
    private static final String CLOSE_TITLE_AND_OPEN_FORM = "</title></head><body><h1>TGT Created</h1><form action=\"";
    private static final String DOCTYPE_AND_TITLE = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>";
    private static final String TGT_CREATED_TITLE_CONTENT = HttpStatus.CREATED.toString() + ' ' + HttpStatus.CREATED.getReasonPhrase();
    private static final String DOCTYPE_AND_OPENING_FORM = DOCTYPE_AND_TITLE + TGT_CREATED_TITLE_CONTENT + CLOSE_TITLE_AND_OPEN_FORM;
    private static final String REST_OF_THE_FORM_AND_CLOSING_TAGS = "\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\"><br><input type=\"submit\" value=\"Submit\"></form></body></html>";
    private static final int SUCCESSFUL_TGT_CREATED_INITIAL_LENGTH = DOCTYPE_AND_OPENING_FORM.length() + REST_OF_THE_FORM_AND_CLOSING_TAGS.length();

    private CentralAuthenticationService centralAuthenticationService;

    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    private CredentialFactory credentialFactory = new DefaultCredentialFactory();

    private ServiceFactory webApplicationServiceFactory;

    private TicketRegistrySupport ticketRegistrySupport;

    private final ObjectWriter jacksonPrettyWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request     raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     * @throws JsonProcessingException in case of JSON parsing failure
     */
    @RequestMapping(value = "/v1/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public DeferredResult<ResponseEntity<String>> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                                            final HttpServletRequest request) throws JsonProcessingException {

        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        CompletableFuture
                .supplyAsync(() -> getResponseEntity(requestBody, request))
                .whenComplete((stringResponseEntity, throwable) -> deferredResult.setResult(stringResponseEntity));

        return deferredResult;
    }

    private ResponseEntity<String> getResponseEntity(@RequestBody MultiValueMap<String, String> requestBody, HttpServletRequest request) {
        try {
            final Credential credential = this.credentialFactory.fromRequestBody(requestBody);
            final AuthenticationResult authenticationResult =
                    this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(null, credential);

            final TicketGrantingTicket tgtId = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
            // TODO: find a better way to create the URI
            final URI ticketReference = new URI(request.getRequestURL().toString() + '/' + tgtId.getId());
            final HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ticketReference);
            headers.setContentType(MediaType.TEXT_HTML);
            String tgtUrl = ticketReference.toString();
            String response = new StringBuilder(SUCCESSFUL_TGT_CREATED_INITIAL_LENGTH + tgtUrl.length())
                    .append(DOCTYPE_AND_OPENING_FORM)
                    .append(tgtUrl)
                    .append(REST_OF_THE_FORM_AND_CLOSING_TAGS)
                    .toString();
            return new ResponseEntity<>(response, headers, HttpStatus.CREATED);

        } catch (final AuthenticationException e) {
            final List<String> authnExceptions = e.getHandlerErrors().values().stream()
                    .map(Class::getSimpleName)
                    .collect(Collectors.toList());
            final Map<String, List<String>> errorsMap = new HashMap<>();
            errorsMap.put("authentication_exceptions", authnExceptions);
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(String.format("Caused by: %s", authnExceptions));
            try {
                return new ResponseEntity<>(this.jacksonPrettyWriter.writeValueAsString(errorsMap), HttpStatus.UNAUTHORIZED);
            } catch (JsonProcessingException e1) {
                LOGGER.error(e.getMessage(), e);
                // TODO: could we create constants ResponseEntity?
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (final BadRequestException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Determine the status of a given ticket id, whether it's valid, exists, expired, etc.
     *
     * @param id ticket id
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/v1/tickets/{id:.+}", method = RequestMethod.GET)
    public ResponseEntity<String> getTicketStatus(@PathVariable("id") final String id) {
        try {
            this.centralAuthenticationService.getTicket(id);
            return new ResponseEntity<>(id, HttpStatus.OK);
        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>("Ticket could not be found", HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create new service ticket.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId       ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/v1/tickets/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                      @PathVariable("tgtId") final String tgtId) {
        try {
            final String serviceId = requestBody.getFirst(CasProtocolConstants.PARAMETER_SERVICE);
            final AuthenticationResultBuilder builder = new DefaultAuthenticationResultBuilder(
                    this.authenticationSystemSupport.getPrincipalElectionStrategy());

            final Service service = this.webApplicationServiceFactory.createService(serviceId);
            final AuthenticationResult authenticationResult =
                    builder.collect(this.ticketRegistrySupport.getAuthenticationFrom(tgtId)).build(service);

            final ServiceTicket serviceTicketId = this.centralAuthenticationService.grantServiceTicket(tgtId,
                    service, authenticationResult);
            return new ResponseEntity<>(serviceTicketId.getId(), HttpStatus.OK);

        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>("TicketGrantingTicket could not be found", HttpStatus.NOT_FOUND);
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
    @RequestMapping(value = "/v1/tickets/{tgtId:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId) {
        this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
        return new ResponseEntity<>(tgtId, HttpStatus.OK);
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    public void setWebApplicationServiceFactory(final ServiceFactory webApplicationServiceFactory) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    public void setTicketRegistrySupport(final TicketRegistrySupport ticketRegistrySupport) {
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return this.authenticationSystemSupport;
    }

    public CredentialFactory getCredentialFactory() {
        return this.credentialFactory;
    }

    public ServiceFactory getWebApplicationServiceFactory() {
        return this.webApplicationServiceFactory;
    }

    public TicketRegistrySupport getTicketRegistrySupport() {
        return this.ticketRegistrySupport;
    }

    public void setCredentialFactory(final CredentialFactory credentialFactory) {
        this.credentialFactory = credentialFactory;
    }


}
