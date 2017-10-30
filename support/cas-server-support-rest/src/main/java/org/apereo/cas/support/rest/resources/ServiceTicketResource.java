package org.apereo.cas.support.rest.resources;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RestController("serviceTicketResourceRestController")
public class ServiceTicketResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTicketResource.class);

    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServiceFactory webApplicationServiceFactory;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory;

    public ServiceTicketResource(final AuthenticationSystemSupport authenticationSystemSupport,
                                 final TicketRegistrySupport ticketRegistrySupport,
                                 final ServiceFactory webApplicationServiceFactory,
                                 final ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory) {
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.serviceTicketResourceEntityResponseFactory = serviceTicketResourceEntityResponseFactory;
    }

    /**
     * Create new service ticket.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId       ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response
     */
    @PostMapping(value = "/v1/tickets/{tgtId:.+}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                      @PathVariable("tgtId") final String tgtId) {
        try {
            final Authentication authn = this.ticketRegistrySupport.getAuthenticationFrom(tgtId);
            final String serviceId = requestBody.getFirst(CasProtocolConstants.PARAMETER_SERVICE);
            if (authn == null) {
                throw new InvalidTicketException(tgtId);
            }
            if (StringUtils.isBlank(serviceId)) {
                throw new InvalidTicketException(serviceId);
            }
            final AuthenticationResultBuilder builder = new DefaultAuthenticationResultBuilder(this.authenticationSystemSupport.getPrincipalElectionStrategy());

            final Service service = this.webApplicationServiceFactory.createService(serviceId);
            final AuthenticationResult authenticationResult = builder.collect(authn).build(service);
            return this.serviceTicketResourceEntityResponseFactory.build(tgtId, service, authenticationResult);
        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>(e.getMessage() + " could not be found", HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
