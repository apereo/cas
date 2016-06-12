package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link RestController} implementation of CAS' REST API.
 *
 * This class implements main CAS RESTful resource for vending/deleting TGTs and vending STs:
 *
 * <ul>
 * <li>{@code POST /v1/tickets}</li>
 * <li>{@code POST /v1/tickets/{TGT-id}}</li>
 * <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("ticketResourceRestController")
public class TicketsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResource.class);
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    
    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Autowired(required = false)
    private CredentialFactory credentialFactory = new DefaultCredentialFactory();

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport = new DefaultTicketRegistrySupport();

    private final ObjectMapper jacksonObjectMapper = new ObjectMapper();


    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request raw HttpServletRequest used to call this method
     *
     * @return ResponseEntity representing RESTful response
     *
     * @throws JsonProcessingException in case of JSON parsing failure
     */
    @RequestMapping(value = "/v1/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                                   final HttpServletRequest request) throws JsonProcessingException {
        try (Formatter fmt = new Formatter()) {

            final Credential credential = this.credentialFactory.fromRequestBody(requestBody);
            final AuthenticationResult authenticationResult =
                    this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(null, credential);

            final TicketGrantingTicket tgtId = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
            final URI ticketReference = new URI(request.getRequestURL().toString() + '/' + tgtId.getId());
            final HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ticketReference);
            headers.setContentType(MediaType.TEXT_HTML);
            fmt.format("<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            fmt.format("%s %s", HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase())
                    .format("</title></head><body><h1>TGT Created</h1><form action=\"%s", ticketReference.toString())
                    .format("\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">")
                    .format("<br><input type=\"submit\" value=\"Submit\"></form></body></html>");
            return new ResponseEntity<>(fmt.toString(), headers, HttpStatus.CREATED);

        }
        catch (final AuthenticationException e) {
            final List<String> authnExceptions = e.getHandlerErrors().entrySet().stream()
                    .map(handlerErrorEntry -> handlerErrorEntry.getValue().getSimpleName())
                    .collect(Collectors.toCollection(LinkedList::new));
            final Map<String, List<String>> errorsMap = new HashMap<>();
            errorsMap.put("authentication_exceptions", authnExceptions);
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(String.format("Caused by: %s", authnExceptions));
            return new ResponseEntity<>(this.jacksonObjectMapper
                    .writer()
                    .withDefaultPrettyPrinter()
                    .writeValueAsString(errorsMap), HttpStatus.UNAUTHORIZED);
        } catch (final BadRequestException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create new service ticket.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId ticket granting ticket id URI path param
     *
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
     *
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

    /**
     * Default implementation of CredentialFactory.
     */
    private static class DefaultCredentialFactory implements CredentialFactory {
        @Override
        public Credential fromRequestBody(final MultiValueMap<String, String> requestBody) {
            final String username = requestBody.getFirst(USERNAME);
            final String password = requestBody.getFirst(PASSWORD);
            if (username == null || password == null) {
                throw new BadRequestException("Invalid payload. 'username' and 'password' form fields are required.");
            }
            return new UsernamePasswordCredential(requestBody.getFirst(USERNAME), requestBody.getFirst(PASSWORD));
        }
    }

    /**
     * Exception to indicate bad payload.
     */
    private static class BadRequestException extends IllegalArgumentException {
        private static final long serialVersionUID = 6852720596988243487L;

        /**
         * Ctor.
         *
         * @param msg error message
         */
        BadRequestException(final String msg) {
            super(msg);
        }
    }
}
