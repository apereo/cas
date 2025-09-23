package org.apereo.cas.support.rest.resources;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.ArgumentExtractor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * CAS RESTful resource for vending STs.
 *
 * <ul>
 * <li>{@code POST /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("serviceTicketResourceRestController")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "CAS REST")
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
    @PostMapping(value = RestProtocolConstants.ENDPOINT_TICKETS + "/{tgtId:.+}",
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
    @Operation(summary = "Create service ticket",
        parameters = {
            @Parameter(name = "tgtId", required = true, in = ParameterIn.PATH, description = "Ticket-granting ticket id"),
            @Parameter(name = "requestBody", required = false, description = "Request body containing credentials")
        })
    public ResponseEntity<String> createServiceTicket(
        final HttpServletRequest httpServletRequest,
        @RequestBody(required = false)
        final MultiValueMap<String, String> requestBody,
        @PathVariable("tgtId")
        final String tgtId) {
        try {
            val authn = ticketRegistrySupport.getAuthenticationFrom(StringEscapeUtils.escapeHtml4(tgtId));
            if (authn == null) {
                throw new InvalidTicketException(tgtId);
            }
            val service = Objects.requireNonNull(argumentExtractor.extractService(httpServletRequest),
                "Target service/application is unspecified or unrecognized in the request");
            if (BooleanUtils.toBoolean(httpServletRequest.getParameter(CasProtocolConstants.PARAMETER_RENEW))) {
                val credential = credentialFactory.fromRequest(httpServletRequest, requestBody);
                if (credential == null || credential.isEmpty()) {
                    throw new BadRestRequestException("No credentials are provided or extracted to authenticate the REST request");
                }
                val authenticationResult = authenticationSystemSupport.finalizeAuthenticationTransaction(service, credential);
                return serviceTicketResourceEntityResponseFactory.build(tgtId, service, authenticationResult);
            }
            val builder = authenticationSystemSupport.getAuthenticationResultBuilderFactory().newBuilder();
            val authenticationResult = builder.collect(authn).build(service);
            return serviceTicketResourceEntityResponseFactory.build(tgtId, service, authenticationResult);
        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(tgtId) + " could not be found or is considered invalid", HttpStatus.NOT_FOUND);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e, httpServletRequest, applicationContext);
        } catch (final BadRestRequestException e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
