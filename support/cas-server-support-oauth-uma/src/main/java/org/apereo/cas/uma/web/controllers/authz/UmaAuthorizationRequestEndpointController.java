package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.uma.ticket.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link UmaAuthorizationRequestEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Controller("umaAuthorizationRequestEndpointController")
public class UmaAuthorizationRequestEndpointController extends BaseUmaEndpointController {
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;

    public UmaAuthorizationRequestEndpointController(final UmaPermissionTicketFactory umaPermissionTicketFactory,
                                                     final ResourceSetRepository umaResourceSetRepository,
                                                     final CasConfigurationProperties casProperties,
                                                     final ServicesManager servicesManager,
                                                     final TicketRegistry ticketRegistry) {
        super(umaPermissionTicketFactory, umaResourceSetRepository, casProperties);
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * Handle authorization request.
     *
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/" + OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleAuthorizationRequest(@RequestBody final String body,
                                                     final HttpServletRequest request, final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response);
            val umaRequest = MAPPER.readValue(body, UmaAuthorizationRequest.class);

            if (StringUtils.isBlank(umaRequest.getRpt()) || StringUtils.isBlank(umaRequest.getTicket())) {
                return new ResponseEntity("Unable to accept authorization request; ticket and/or rpt parameters are missing", HttpStatus.BAD_REQUEST);
            }
            val at = this.ticketRegistry.getTicket(umaRequest.getRpt(), AccessToken.class);
            if (at == null || at.isExpired()) {
                return new ResponseEntity("Access token is invalid or has expired", HttpStatus.BAD_REQUEST);
            }
            val permissionTicket = this.ticketRegistry.getTicket(umaRequest.getTicket(), UmaPermissionTicket.class);
            if (permissionTicket == null || permissionTicket.isExpired()) {
                return new ResponseEntity("Permission ticket is invalid or has expired", HttpStatus.BAD_REQUEST);
            }
            val resourceSet = permissionTicket.getResourceSet();
            if (resourceSet == null || resourceSet.getPolicies() == null || resourceSet.getPolicies().isEmpty()) {
                return new ResponseEntity("resource-set or linked policies are undefined", HttpStatus.BAD_REQUEST);
            }

            // Check to see if claims are satisfied
            for (val policy : resourceSet.getPolicies()) {
                for (val perm : policy.getPermissions()) {
                    Collection<Claim> unmatched = checkIndividualClaims(resourceSet.get, permissionTicket.getClaims());
                }

                if (unmatched.isEmpty()) {
                    // we found something that's satisfied the claims, let's go with it!
                    return new ClaimProcessingResult(policy);
                } else {
                    // otherwise add it to the stack to send back
                    allUnmatched.addAll(unmatched);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity("Unable to handle authorization request", HttpStatus.BAD_REQUEST);
    }
}
