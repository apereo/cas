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
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20CallbackUrlBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
    protected OAuthCodeFactory oAuthCodeFactory;

    /**
     * The Consent approval view resolver.
     */
    protected final ConsentApprovalViewResolver consentApprovalViewResolver;

    /**
     * The Authentication builder.
     */
    protected final OAuth20CasAuthenticationBuilder authenticationBuilder;

    /**
     * Collection of callback builders.
     */
    protected final Set<OAuth20CallbackUrlBuilder> calbackUrlBuilders;

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
                                              final Set<OAuth20CallbackUrlBuilder> calbackUrlBuilders) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties,
                ticketGrantingTicketCookieGenerator);
        this.oAuthCodeFactory = oAuthCodeFactory;
        this.consentApprovalViewResolver = consentApprovalViewResolver;
        this.authenticationBuilder = authenticationBuilder;
        this.calbackUrlBuilders = calbackUrlBuilders;
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);

        if (!verifyAuthorizeRequest(request) || !isRequestAuthenticated(manager, context)) {
            LOGGER.error("Authorize request verification failed");
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
     * @throws Exception the exception
     */
    protected ModelAndView redirectToCallbackRedirectUrl(final ProfileManager manager,
                                                         final OAuthRegisteredService registeredService,
                                                         final J2EContext context,
                                                         final String clientId) throws Exception {
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

        final String callbackUrl = buildCallbackUrlForRequest(registeredService, context, clientId, service, authentication);

        LOGGER.debug("Callback URL to redirect: [{}]", callbackUrl);
        if (StringUtils.isBlank(callbackUrl)) {
            return OAuth20Utils.produceUnauthorizedErrorView();
        }
        return OAuth20Utils.redirectTo(callbackUrl);
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
    protected String buildCallbackUrlForRequest(final OAuthRegisteredService registeredService, final J2EContext context,
                                              final String clientId, final Service service,
                                              final Authentication authentication) {
        final OAuth20CallbackUrlBuilder builder = this.calbackUrlBuilders
                .stream()
                .filter(b -> b.supports(context))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not build the callback url. Response type likely not supported"));

        final TicketGrantingTicket ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                ticketGrantingTicketCookieGenerator, this.ticketRegistry, context.getRequest());
        final AccessTokenRequestDataHolder holder = new AccessTokenRequestDataHolder(service, authentication,
                registeredService, ticketGrantingTicket, OAuth20GrantTypes.IMPLICIT);

        return builder.build(context, clientId, holder);
    }

    /**
     * Verify the authorize request.
     *
     * @param request the HTTP request
     * @return whether the authorize request is valid
     */
    private boolean verifyAuthorizeRequest(final HttpServletRequest request) {

        final boolean checkParameterExist = this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                && this.validator.checkParameterExist(request, OAuth20Constants.REDIRECT_URI)
                && this.validator.checkParameterExist(request, OAuth20Constants.RESPONSE_TYPE);

        final String responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);

        return checkParameterExist
                && checkResponseTypes(responseType, OAuth20ResponseTypes.values())
                && this.validator.checkServiceValid(registeredService)
                && this.validator.checkCallbackValid(registeredService, redirectUri);
    }

    /**
     * Check the response type against expected response types.
     *
     * @param type          the current response type
     * @param expectedTypes the expected response types
     * @return whether the response type is supported
     */
    private static boolean checkResponseTypes(final String type, final OAuth20ResponseTypes... expectedTypes) {
        LOGGER.debug("Response type: [{}]", type);
        final boolean checked = Stream.of(expectedTypes).anyMatch(t -> OAuth20Utils.isResponseType(type, t));
        if (!checked) {
            LOGGER.error("Unsupported response type: [{}]", type);
        }
        return checked;
    }
}


