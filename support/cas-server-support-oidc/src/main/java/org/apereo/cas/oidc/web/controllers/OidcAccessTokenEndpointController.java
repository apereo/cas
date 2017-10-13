package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link OidcAccessTokenEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAccessTokenEndpointController extends OAuth20AccessTokenEndpointController {

    public OidcAccessTokenEndpointController(final ServicesManager servicesManager,
                                             final TicketRegistry ticketRegistry,
                                             final OAuth20Validator validator,
                                             final AccessTokenFactory accessTokenFactory,
                                             final PrincipalFactory principalFactory,
                                             final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                             final OAuth20TokenGenerator accessTokenGenerator,
                                             final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                             final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                             final CasConfigurationProperties casProperties,
                                             final CookieRetrievingCookieGenerator cookieGenerator,
                                             final ExpirationPolicy accessTokenExpirationPolicy,
                                             final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, accessTokenGenerator, accessTokenResponseGenerator,
                scopeToAttributesFilter, casProperties, cookieGenerator, 
                accessTokenExpirationPolicy, accessTokenGrantRequestExtractors);
    }

    @PostMapping(value = {'/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.TOKEN_URL})
    @Override
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        super.handleRequest(request, response);
    }

    @GetMapping(value = {'/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.TOKEN_URL})
    @Override
    public void handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        super.handleRequest(request, response);
    }
}
