package org.apereo.cas.support.oauth.web.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20RequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

/**
 * This controller is in charge of responding to the authorize call in OAuth v2 protocol.
 * This url is protected by a CAS authentication. It returns an OAuth code or directly an access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20AuthorizeEndpointController extends BaseOAuth20Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeEndpointController.class);

    /**
     * The code factory instance.
     */
    protected final OAuthCodeFactory oAuthCodeFactory;

    /**
     * The Consent approval view resolver.
     */
    protected final ConsentApprovalViewResolver consentApprovalViewResolver;

    /**
     * The Authentication builder.
     */
    protected final OAuth20CasAuthenticationBuilder authenticationBuilder;

    /**
     * Collection of response builders.
     */
    protected final Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders;

    /**
     * Collection of request validators.
     */
    protected final Set<OAuth20RequestValidator> oauthRequestValidators;

    public OAuth20AuthorizeEndpointController(final ServicesManager servicesManager,
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
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties,
                ticketGrantingTicketCookieGenerator);
        this.oAuthCodeFactory = oAuthCodeFactory;
        this.consentApprovalViewResolver = consentApprovalViewResolver;
        this.authenticationBuilder = authenticationBuilder;
        this.oauthAuthorizationResponseBuilders = oauthAuthorizationResponseBuilders;
        this.oauthRequestValidators = oauthRequestValidators;
    }

    /**
     * Handle request via GET.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final J2EContext context = Pac4jUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = Pac4jUtils.getPac4jProfileManager(request, response);

        if (!verifyAuthorizeRequest(context) || !isRequestAuthenticated(manager, context)) {
            LOGGER.error("Authorize request verification failed. Either the authorization request is missing required parameters, "
                    + "or the request is not authenticated and contains no authenticated profile/principal.");
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        final String clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientId, registeredService);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        final ModelAndView mv = this.consentApprovalViewResolver.resolve(context, registeredService);
        if (!mv.isEmpty() && mv.hasView()) {
            return mv;
        }

        return redirectToCallbackRedirectUrl(manager, registeredService, context, clientId);
    }

    /**
     * Handle request post.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }
    
    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
    }

    private static boolean isRequestAuthenticated(final ProfileManager manager, final J2EContext context) {
        final Optional<CommonProfile> opt = manager.get(true);
        return opt.isPresent();
    }

    /**
     * Redirect to callback redirect url model and view.
     *
     * @param manager           the manager
     * @param registeredService the registered service
     * @param context           the context
     * @param clientId          the client id
     * @return the model and view
     */
    protected ModelAndView redirectToCallbackRedirectUrl(final ProfileManager manager,
                                                         final OAuthRegisteredService registeredService,
                                                         final J2EContext context,
                                                         final String clientId) {
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            LOGGER.error("Unexpected null profile from profile manager. Request is not fully authenticated.");
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        final Service service = this.authenticationBuilder.buildService(registeredService, context, false);
        LOGGER.debug("Created service [{}] based on registered service [{}]", service, registeredService);

        final Authentication authentication = this.authenticationBuilder.build(profile.get(), registeredService, context, service);
        LOGGER.debug("Created OAuth authentication [{}] for service [{}]", service, authentication);
        
        try {
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        final View view = buildAuthorizationForRequest(registeredService, context, clientId, service, authentication);
        if (view != null) {
            return OAuth20Utils.redirectTo(view);
        }
        LOGGER.debug("No explicit view was defined as part of the authorization response");
        return null;
    }

    /**
     * Build callback url for request string.
     *
     * @param registeredService the registered service
     * @param context           the context
     * @param clientId          the client id
     * @param service           the service
     * @param authentication    the authentication
     * @return the string
     */
    protected View buildAuthorizationForRequest(final OAuthRegisteredService registeredService,
                                                final J2EContext context,
                                                final String clientId, final Service service,
                                                final Authentication authentication) {
        final OAuth20AuthorizationResponseBuilder builder = this.oauthAuthorizationResponseBuilders
                .stream()
                .filter(b -> b.supports(context))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not build the callback url. Response type likely not supported"));

        final TicketGrantingTicket ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                ticketGrantingTicketCookieGenerator, this.ticketRegistry, context.getRequest());

        final String grantType = StringUtils.defaultIfEmpty(context.getRequestParameter(OAuth20Constants.GRANT_TYPE),
                OAuth20GrantTypes.AUTHORIZATION_CODE.getType()).toUpperCase();
        final Set<String> scopes = OAuth20Utils.parseRequestScopes(context);
        final AccessTokenRequestDataHolder holder = new AccessTokenRequestDataHolder(service, authentication,
                registeredService, ticketGrantingTicket, OAuth20GrantTypes.valueOf(grantType), scopes);
        
        return builder.build(context, clientId, holder);
    }

    /**
     * Verify the authorize request.
     *
     * @param context the context
     * @return whether the authorize request is valid
     */
    private boolean verifyAuthorizeRequest(final J2EContext context) {
        final OAuth20RequestValidator validator = this.oauthRequestValidators
                .stream()
                .filter(b -> b.supports(context))
                .findFirst()
                .orElse(null);
        if (validator == null) {
            LOGGER.warn("Ignoring malformed request [{}] no OAuth20 validator could declare support for its syntax", context.getFullRequestURL());
            return false;
        }
        return validator.validate(context);
    }
}


