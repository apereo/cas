package org.apereo.cas.support.oauth.web.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
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
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                                              final OAuth20CasAuthenticationBuilder authenticationBuilder) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties,
                ticketGrantingTicketCookieGenerator);
        this.oAuthCodeFactory = oAuthCodeFactory;
        this.consentApprovalViewResolver = consentApprovalViewResolver;
        this.authenticationBuilder = authenticationBuilder;
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

        final String redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI);
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);

        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);

        final TicketGrantingTicket ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                ticketGrantingTicketCookieGenerator, this.ticketRegistry, context.getRequest());
        final String callbackUrl;
        if (OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.CODE)) {
            callbackUrl = buildCallbackUrlForAuthorizationCodeResponseType(authentication, service, redirectUri, ticketGrantingTicket);
        } else if (OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.TOKEN)) {
            final AccessTokenRequestDataHolder holder = new AccessTokenRequestDataHolder(service, authentication, 
                    registeredService, ticketGrantingTicket);
            callbackUrl = buildCallbackUrlForImplicitTokenResponseType(holder, redirectUri);
        } else {
            callbackUrl = buildCallbackUrlForTokenResponseType(context, authentication, service, redirectUri, responseType, clientId);
        }

        LOGGER.debug("Callback URL to redirect: [{}]", callbackUrl);
        if (StringUtils.isBlank(callbackUrl)) {
            return OAuth20Utils.produceUnauthorizedErrorView();
        }
        return OAuth20Utils.redirectTo(callbackUrl);
    }

    /**
     * Build callback url for token response type string.
     *
     * @param context        the context
     * @param authentication the authentication
     * @param service        the service
     * @param redirectUri    the redirect uri
     * @param responseType   the response type
     * @param clientId       the client id
     * @return the callback url
     */
    protected String buildCallbackUrlForTokenResponseType(final J2EContext context, final Authentication authentication,
                                                          final Service service,
                                                          final String redirectUri,
                                                          final String responseType,
                                                          final String clientId) {
        return null;
    }

    private String buildCallbackUrlForImplicitTokenResponseType(final AccessTokenRequestDataHolder holder,
                                                                final String redirectUri) throws Exception {
        final AccessToken accessToken = generateAccessToken(holder);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(holder.getAuthentication(),
                holder.getService(), redirectUri, accessToken, Collections.emptyList());
    }

    /**
     * Build callback url response type string.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param redirectUri    the redirect uri
     * @param accessToken    the access token
     * @param params         the params
     * @return the string
     * @throws Exception the exception
     */
    protected String buildCallbackUrlResponseType(final Authentication authentication,
                                                  final Service service,
                                                  final String redirectUri,
                                                  final AccessToken accessToken,
                                                  final List<NameValuePair> params) throws Exception {
        final String state = authentication.getAttributes().get(OAuth20Constants.STATE).toString();
        final String nonce = authentication.getAttributes().get(OAuth20Constants.NONCE).toString();

        final URIBuilder builder = new URIBuilder(redirectUri);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OAuth20Constants.ACCESS_TOKEN)
                .append('=')
                .append(accessToken.getId())
                .append('&')
                .append(OAuth20Constants.TOKEN_TYPE)
                .append('=')
                .append(OAuth20Constants.TOKEN_TYPE_BEARER)
                .append('&')
                .append(OAuth20Constants.EXPIRES_IN)
                .append('=')
                .append(casProperties.getTicket().getTgt().getTimeToKillInSeconds());

        params.forEach(p -> stringBuilder.append('&')
                .append(p.getName())
                .append('=')
                .append(p.getValue()));

        if (StringUtils.isNotBlank(state)) {
            stringBuilder.append('&')
                    .append(OAuth20Constants.STATE)
                    .append('=')
                    .append(EncodingUtils.urlEncode(state));
        }
        if (StringUtils.isNotBlank(nonce)) {
            stringBuilder.append('&')
                    .append(OAuth20Constants.NONCE)
                    .append('=')
                    .append(EncodingUtils.urlEncode(nonce));
        }
        builder.setFragment(stringBuilder.toString());
        final String url = builder.toString();
        return url;
    }

    private String buildCallbackUrlForAuthorizationCodeResponseType(final Authentication authentication,
                                                                    final Service service, final String redirectUri,
                                                                    final TicketGrantingTicket ticketGrantingTicket) {

        final OAuthCode code = this.oAuthCodeFactory.create(service, authentication, ticketGrantingTicket);
        LOGGER.debug("Generated OAuth code: [{}]", code);
        this.ticketRegistry.addTicket(code);

        final String state = authentication.getAttributes().get(OAuth20Constants.STATE).toString();
        final String nonce = authentication.getAttributes().get(OAuth20Constants.NONCE).toString();

        String callbackUrl = redirectUri;
        callbackUrl = CommonHelper.addParameter(callbackUrl, OAuth20Constants.CODE, code.getId());
        if (StringUtils.isNotBlank(state)) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuth20Constants.STATE, state);
        }
        if (StringUtils.isNotBlank(nonce)) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuth20Constants.NONCE, nonce);
        }
        return callbackUrl;
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


