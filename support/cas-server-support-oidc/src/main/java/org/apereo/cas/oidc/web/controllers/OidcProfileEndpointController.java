package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileControllerController;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
                                         final CasConfigurationProperties casProperties) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties);
    }

    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected Map<String, Object> writeOutProfileResponse(final Authentication authentication, final Principal principal) throws IOException {
        final Map<String, Object> map = new HashMap<>(principal.getAttributes());
        if (!map.containsKey(OidcConstants.CLAIM_SUB)) {
            map.put(OidcConstants.CLAIM_SUB, principal.getId());
        }
        map.put(OidcConstants.CLAIM_AUTH_TIME, authentication.getAuthenticationDate().toEpochSecond());
        return map;
    }
}
