package org.jasig.cas.support.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationContextBuilder;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationContextBuilder;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.jasig.cas.ticket.registry.TicketRegistrySupport;
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
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link RestController} implementation of CAS' REST API.
 *
 * This class implements main CAS RESTful resource for vending/deleting TGTs and vending STs:
 *
 * <ul>
 *     <li>{@code POST /v1/tickets}</li>
 *     <li>{@code POST /v1/tickets/{TGT-id}}</li>
 *     <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("ticketResourceRestController")
public class TicketsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResource.class);

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Autowired(required = false)
    private final CredentialFactory credentialFactory = new DefaultCredentialFactory();

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSupport")
    private TicketRegistrySupport ticketRegistrySupport = new DefaultTicketRegistrySupport();

    private final ObjectMapper jacksonObjectMapper = new ObjectMapper();


    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     * @throws JsonProcessingException in case of JSON parsing failure
     */
    @RequestMapping(value = "/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                                   final HttpServletRequest request) throws JsonProcessingException {
        try (Formatter fmt = new Formatter()) {

            final Credential credential = this.credentialFactory.fromRequestBody(requestBody);

            final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                    this.authenticationSystemSupport.getPrincipalElectionStrategy());
            final AuthenticationTransaction transaction =
                    AuthenticationTransaction.wrap(credential);
            this.authenticationSystemSupport.getAuthenticationTransactionManager().handle(transaction,  builder);
            final AuthenticationContext authenticationContext = builder.build();

            final TicketGrantingTicket tgtId = this.centralAuthenticationService.createTicketGrantingTicket(authenticationContext);
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
        catch(final AuthenticationException e) {
            final List<String> authnExceptions = new LinkedList<>();
            for (final Map.Entry<String, Class<? extends Exception>> handlerErrorEntry: e.getHandlerErrors().entrySet()) {
                authnExceptions.add(handlerErrorEntry.getValue().getSimpleName());
            }
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
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/tickets/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                            @PathVariable("tgtId") final String tgtId) {
        try {
            final String serviceId = requestBody.getFirst(CasProtocolConstants.PARAMETER_SERVICE);
            final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                    this.authenticationSystemSupport.getPrincipalElectionStrategy());

            final Service service = this.webApplicationServiceFactory.createService(serviceId);
            final AuthenticationContext authenticationContext =
                    builder.collect(this.ticketRegistrySupport.getAuthenticationFrom(tgtId)).build(service);

            final ServiceTicket serviceTicketId = this.centralAuthenticationService.grantServiceTicket(tgtId,
                    service, authenticationContext);
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
    @RequestMapping(value = "/tickets/{tgtId:.+}", method = RequestMethod.DELETE)
    public final ResponseEntity<String> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId) {
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
        return centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return authenticationSystemSupport;
    }

    public CredentialFactory getCredentialFactory() {
        return credentialFactory;
    }

    public ServiceFactory getWebApplicationServiceFactory() {
        return webApplicationServiceFactory;
    }

    public TicketRegistrySupport getTicketRegistrySupport() {
        return ticketRegistrySupport;
    }

    /**
     * Default implementation of CredentialFactory.
     */
    private static class DefaultCredentialFactory implements CredentialFactory {
        @Override
        public Credential fromRequestBody(@NotNull final MultiValueMap<String, String> requestBody) {
            final String username = requestBody.getFirst("username");
            final String password = requestBody.getFirst("password");
            if(username == null || password == null) {
                throw new BadRequestException("Invalid payload. 'username' and 'password' form fields are required.");
            }
            return new UsernamePasswordCredential(requestBody.getFirst("username"), requestBody.getFirst("password"));
        }
    }

    /**
     * Exception to indicate bad payload.
     */
    private static class BadRequestException extends IllegalArgumentException {
        private static final long serialVersionUID = 6852720596988243487L;

        /**
         * Ctor.
         * @param msg error message
         */
        BadRequestException(final String msg) {
            super(msg);
        }
    }
}
