package org.apereo.cas.support.rest.resources;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
@Slf4j
@RequiredArgsConstructor
public class ServiceTicketResource {
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final ArgumentExtractor argumentExtractor;

    private final ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory;

    private final RestHttpRequestCredentialFactory credentialFactory;

    private final ApplicationContext applicationContext;

    /**
     * Create new service ticket.
     *
     * @param httpServletRequest http request
     * @param requestBody        request body
     * @param tgtId              ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response
     */
    @PostMapping(value = "/v1/tickets/{tgtId:.+}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createServiceTicket(final HttpServletRequest httpServletRequest,
                                                      @RequestBody(required = false) final MultiValueMap<String, String> requestBody,
                                                      @PathVariable("tgtId") final String tgtId) {
        try {
            val authn = this.ticketRegistrySupport.getAuthenticationFrom(tgtId);
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authn);
            if (authn == null) {
                throw new InvalidTicketException(tgtId);
            }
            val service = this.argumentExtractor.extractService(httpServletRequest);
            if (service == null) {
                throw new IllegalArgumentException("Target service/application is unspecified or unrecognized in the request");
            }
            if (BooleanUtils.toBoolean(httpServletRequest.getParameter(CasProtocolConstants.PARAMETER_RENEW))) {
                val credential = this.credentialFactory.fromRequest(httpServletRequest, requestBody);
                if (credential == null || credential.isEmpty()) {
                    throw new BadRestRequestException("No credentials are provided or extracted to authenticate the REST request");
                }
                val authenticationResult =
                    authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

                return this.serviceTicketResourceEntityResponseFactory.build(tgtId, service, authenticationResult);
            }
            val builder = new DefaultAuthenticationResultBuilder();
            val authenticationResult = builder
                .collect(authn)
                .build(this.authenticationSystemSupport.getPrincipalElectionStrategy(), service);
            return this.serviceTicketResourceEntityResponseFactory.build(tgtId, service, authenticationResult);

        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>(tgtId + " could not be found or is considered invalid", HttpStatus.NOT_FOUND);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e, httpServletRequest, applicationContext);
        } catch (final BadRestRequestException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            AuthenticationCredentialsThreadLocalBinder.clear();
        }
    }
}
