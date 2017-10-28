package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileControllerController;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcProfileEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcProfileEndpointController extends OAuth20UserProfileControllerController {

    public OidcProfileEndpointController(final ServicesManager servicesManager,
                                         final TicketRegistry ticketRegistry,
                                         final OAuth20Validator validator,
                                         final AccessTokenFactory accessTokenFactory,
                                         final PrincipalFactory principalFactory,
                                         final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                         final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                         final CasConfigurationProperties casProperties,
                                         final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                         final OAuth20UserProfileViewRenderer userProfileViewRenderer) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties,
                ticketGrantingTicketCookieGenerator, userProfileViewRenderer);
    }

    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<String> handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequest(request, response);
    }

    @Override
    protected Map<String, Object> writeOutProfileResponse(final AccessToken accessToken) {
        final Principal principal = accessToken.getAuthentication().getPrincipal();
        final Map<String, Object> map = new HashMap<>(principal.getAttributes());
        if (!map.containsKey(OidcConstants.CLAIM_SUB)) {
            map.put(OidcConstants.CLAIM_SUB, principal.getId());
        }
        map.put(OidcConstants.CLAIM_AUTH_TIME, accessToken.getAuthentication().getAuthenticationDate().toEpochSecond());
        return map;
    }
}
