package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20RequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Set;

/**
 * This is {@link OidcAuthorizeEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAuthorizeEndpointController extends OAuth20AuthorizeEndpointController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthorizeEndpointController.class);

    public OidcAuthorizeEndpointController(final ServicesManager servicesManager,
                                           final TicketRegistry ticketRegistry,
                                           final OAuth20Validator validator,
                                           final AccessTokenFactory accessTokenFactory,
                                           final PrincipalFactory principalFactory,
                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                           final OAuthCodeFactory oAuthCodeFactory,
                                           final ConsentApprovalViewResolver consentApprovalViewResolver,
                                           final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                           final CasConfigurationProperties casProperties,
                                           final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                           final OAuth20CasAuthenticationBuilder authenticationBuilder,
                                           final Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders,
                                           final Set<OAuth20RequestValidator> oauthRequestValidators) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, oAuthCodeFactory, consentApprovalViewResolver,
                scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator,
                authenticationBuilder, oauthAuthorizationResponseBuilders, oauthRequestValidators);
    }

    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final Collection<String> scopes = OAuth20Utils.getRequestedScopes(request);
        if (scopes.isEmpty() || !scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Provided scopes [{}] are undefined by OpenID Connect, which requires that scope [{}] MUST be specified, "
                            + "or the behavior is unspecified. CAS MAY allow this request to be processed for now.",
                    scopes, OidcConstants.StandardScopes.OPENID.getScope());
        }

        return super.handleRequest(request, response);
    }

    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    @Override
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }

    @Override
    protected View buildAuthorizationForRequest(final OAuthRegisteredService registeredService, final J2EContext context,
                                                final String clientId, final Service service, final Authentication authentication) {
        return super.buildAuthorizationForRequest(registeredService, context, clientId, service, authentication);
    }
}
