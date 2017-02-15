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
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
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
public class OAuth20AuthorizeEndpointController extends BaseOAuthWrapperController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeEndpointController.class);

    /**
     * The code factory instance.
     */
    protected OAuthCodeFactory oAuthCodeFactory;

    private ConsentApprovalViewResolver consentApprovalViewResolver;

    public OAuth20AuthorizeEndpointController(final ServicesManager servicesManager,
                                              final TicketRegistry ticketRegistry,
                                              final OAuth20Validator validator,
                                              final AccessTokenFactory accessTokenFactory,
                                              final PrincipalFactory principalFactory,
                                              final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                              final OAuthCodeFactory oAuthCodeFactory,
                                              final ConsentApprovalViewResolver consentApprovalViewResolver,
                                              final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                              final CasConfigurationProperties casProperties) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties);
        this.oAuthCodeFactory = oAuthCodeFactory;
        this.consentApprovalViewResolver = consentApprovalViewResolver;
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.AUTHORIZE_URL)
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);

        if (!verifyAuthorizeRequest(request) || !isRequestAuthenticated(manager, context)) {
            LOGGER.error("Authorize request verification failed");
            return OAuthUtils.produceUnauthorizedErrorView();
        }

        final String clientId = context.getRequestParameter(OAuthConstants.CLIENT_ID);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientId, registeredService);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return OAuthUtils.produceUnauthorizedErrorView();
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
        return OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);
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
            return OAuthUtils.produceUnauthorizedErrorView();
        }

        final Service service = createService(registeredService, context);
        LOGGER.debug("Created service [{}] based on registered service [{}]", service, registeredService);

        final Authentication authentication = createAuthentication(profile.get(), registeredService, context, service);
        LOGGER.debug("Created OAuth authentication [{}] for service [{}]", service, authentication);

        try {
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            LOGGER.error(e.getMessage(), e);
            return OAuthUtils.produceUnauthorizedErrorView();
        }

        final String redirectUri = context.getRequestParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);

        final String responseType = context.getRequestParameter(OAuthConstants.RESPONSE_TYPE);

        final String callbackUrl;
        if (isResponseType(responseType, OAuth20ResponseTypes.CODE)) {
            callbackUrl = buildCallbackUrlForAuthorizationCodeResponseType(authentication, service, redirectUri);
        } else if (isResponseType(responseType, OAuth20ResponseTypes.TOKEN)) {
            callbackUrl = buildCallbackUrlForImplicitTokenResponseType(context, authentication, service, redirectUri);
        } else {
            callbackUrl = buildCallbackUrlForTokenResponseType(context, authentication, service,
                    redirectUri, responseType, clientId);
        }

        LOGGER.debug("callbackUrl: [{}]", callbackUrl);
        if (StringUtils.isBlank(callbackUrl)) {
            return OAuthUtils.produceUnauthorizedErrorView();
        }
        return OAuthUtils.redirectTo(callbackUrl);
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

    private String buildCallbackUrlForImplicitTokenResponseType(final J2EContext context,
                                                                final Authentication authentication,
                                                                final Service service,
                                                                final String redirectUri) throws Exception {

        final AccessToken accessToken = generateAccessToken(service, authentication, context);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(authentication, service,
                redirectUri, accessToken, Collections.emptyList());
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
        final String state = authentication.getAttributes().get(OAuthConstants.STATE).toString();
        final String nonce = authentication.getAttributes().get(OAuthConstants.NONCE).toString();

        final URIBuilder builder = new URIBuilder(redirectUri);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OAuthConstants.ACCESS_TOKEN)
                .append('=')
                .append(accessToken.getId())
                .append('&')
                .append(OAuthConstants.TOKEN_TYPE)
                .append('=')
                .append(OAuthConstants.TOKEN_TYPE_BEARER)
                .append('&')
                .append(OAuthConstants.EXPIRES_IN)
                .append('=')
                .append(casProperties.getTicket().getTgt().getTimeToKillInSeconds());

        params.forEach(p -> stringBuilder.append('&')
                .append(p.getName())
                .append('=')
                .append(p.getValue()));

        if (StringUtils.isNotBlank(state)) {
            stringBuilder.append('&')
                    .append(OAuthConstants.STATE)
                    .append('=')
                    .append(EncodingUtils.urlEncode(state));
        }
        if (StringUtils.isNotBlank(nonce)) {
            stringBuilder.append('&')
                    .append(OAuthConstants.NONCE)
                    .append('=')
                    .append(EncodingUtils.urlEncode(nonce));
        }
        builder.setFragment(stringBuilder.toString());
        final String url = builder.toString();
        return url;
    }

    private String buildCallbackUrlForAuthorizationCodeResponseType(final Authentication authentication,
                                                                    final Service service,
                                                                    final String redirectUri) {

        final OAuthCode code = this.oAuthCodeFactory.create(service, authentication);
        LOGGER.debug("Generated OAuth code: [{}]", code);
        getTicketRegistry().addTicket(code);

        final String state = authentication.getAttributes().get(OAuthConstants.STATE).toString();
        final String nonce = authentication.getAttributes().get(OAuthConstants.NONCE).toString();

        String callbackUrl = redirectUri;
        callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.CODE, code.getId());
        if (StringUtils.isNotBlank(state)) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.STATE, state);
        }
        if (StringUtils.isNotBlank(nonce)) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.NONCE, nonce);
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

        final boolean checkParameterExist = getValidator().checkParameterExist(request, OAuthConstants.CLIENT_ID)
                && getValidator().checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                && getValidator().checkParameterExist(request, OAuthConstants.RESPONSE_TYPE);

        final String responseType = request.getParameter(OAuthConstants.RESPONSE_TYPE);
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);

        return checkParameterExist
                && checkResponseTypes(responseType, OAuth20ResponseTypes.values())
                && getValidator().checkServiceValid(registeredService)
                && getValidator().checkCallbackValid(registeredService, redirectUri);
    }

    /**
     * Check the response type against expected response types.
     *
     * @param type          the current response type
     * @param expectedTypes the expected response types
     * @return whether the response type is supported
     */
    private boolean checkResponseTypes(final String type, final OAuth20ResponseTypes... expectedTypes) {
        LOGGER.debug("Response type: [{}]", type);
        final boolean checked = Stream.of(expectedTypes).anyMatch(t -> isResponseType(type, t));
        if (!checked) {
            LOGGER.error("Unsupported response type: [{}]", type);
        }
        return checked;
    }

    /**
     * Check the response type against an expected response type.
     *
     * @param type         the given response type
     * @param expectedType the expected response type
     * @return whether the response type is the expected one
     */
    protected boolean isResponseType(final String type, final OAuth20ResponseTypes expectedType) {
        return expectedType.getType().equalsIgnoreCase(type);
    }
}


