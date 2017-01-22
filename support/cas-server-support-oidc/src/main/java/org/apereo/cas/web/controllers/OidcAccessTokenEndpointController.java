package org.apereo.cas.web.controllers;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenController;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAccessTokenEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAccessTokenEndpointController extends OAuth20AccessTokenController {

    public OidcAccessTokenEndpointController(final ServicesManager servicesManager,
                                             final TicketRegistry ticketRegistry,
                                             final OAuth20Validator validator,
                                             final AccessTokenFactory accessTokenFactory,
                                             final PrincipalFactory principalFactory,
                                             final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                             final RefreshTokenFactory refreshTokenFactory,
                                             final AccessTokenResponseGenerator accessTokenResponseGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, refreshTokenFactory, accessTokenResponseGenerator);
    }

    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL)
    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
