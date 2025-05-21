package org.apereo.cas.uma.web.controllers.claims;

import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link UmaRequestingPartyClaimsCollectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Controller("umaRequestingPartyClaimsCollectionEndpointController")
@Tag(name = "User Managed Access")
public class UmaRequestingPartyClaimsCollectionEndpointController extends BaseUmaEndpointController {
    public UmaRequestingPartyClaimsCollectionEndpointController(final UmaConfigurationContext umaConfigurationContext) {
        super(umaConfigurationContext);
    }

    /**
     * Gets claims.
     *
     * @param clientId    the client id
     * @param redirectUri the redirect uri
     * @param ticketId    the ticket id
     * @param state       the state
     * @param request     the request
     * @param response    the response
     * @return redirect view
     * @throws Exception the exception
     */
    @GetMapping(OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.UMA_CLAIMS_COLLECTION_URL)
    @Operation(
        summary = "Collect claims",
        description = "Collects claims and redirects to the client application",
        parameters = {
            @Parameter(name = "client_id", required = true, description = "Client ID"),
            @Parameter(name = "redirect_uri", required = true, description = "Redirect URI"),
            @Parameter(name = "ticket", required = true, description = "Ticket ID"),
            @Parameter(name = "state", required = false, description = "State")
        }
    )
    public View getClaims(@RequestParam("client_id") final String clientId,
                          @RequestParam("redirect_uri") final String redirectUri,
                          @RequestParam("ticket") final String ticketId,
                          @RequestParam(value = "state", required = false) final String state,
                          final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        val profileResult = getAuthenticatedProfile(request, response, OAuth20Constants.UMA_PROTECTION_SCOPE);

        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(getUmaConfigurationContext().getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service);

        val ticketRegistry = getUmaConfigurationContext().getTicketRegistry();
        val ticket = ticketRegistry.getTicket(ticketId, UmaPermissionTicket.class);
        if (ticket == null || ticket.isExpired()) {
            throw new InvalidTicketException(ticketId);
        }

        ticket.getClaims().putAll(profileResult.getAttributes());
        ticketRegistry.updateTicket(ticket);

        if (StringUtils.isBlank(redirectUri) || !service.matches(redirectUri)) {
            throw UnauthorizedServiceException.denied("Redirect URI is unauthorized for this service definition");
        }

        val template = UriComponentsBuilder.fromUriString(redirectUri);
        template.queryParam(OAuth20Constants.AUTHORIZATION_STATE, OAuth20Constants.CLAIMS_SUBMITTED);
        if (StringUtils.isNotBlank(state)) {
            template.queryParam(OAuth20Constants.STATE, state);
        }

        val redirectTo = template.toUriString();
        return new RedirectView(redirectTo);
    }
}
