package org.apereo.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.apereo.cas.util.EncodingUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This controller is in charge of responding to the authorize call in OAuth v2 protocol.
 * This url is protected by a CAS authentication. It returns an OAuth code or directly an access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20AuthorizeController extends BaseOAuthWrapperController {

    /**
     * The code factory instance.
     */
    protected OAuthCodeFactory oAuthCodeFactory;
    
    private ConsentApprovalViewResolver consentApprovalViewResolver;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.AUTHORIZE_URL)
    public ModelAndView handleRequestInternal(final HttpServletRequest request,
                                              final HttpServletResponse response) throws Exception {

        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        
        if (!verifyAuthorizeRequest(request) || !isRequestAuthenticated(manager, context)) {
            logger.error("Authorize request verification fails");
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        
        final String clientId = context.getRequestParameter(OAuthConstants.CLIENT_ID);
        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientId, registeredService);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        
        final ModelAndView mv = this.consentApprovalViewResolver.resolve(context, registeredService);
        if (!mv.isEmpty() && mv.hasView()) {
            return mv;
        }
        
        return redirectToCallbackRedirectUrl(manager, registeredService, context, clientId);

    }

    private static boolean isRequestAuthenticated(final ProfileManager manager, final J2EContext context) {
        final Optional<CommonProfile> opt = manager.get(true);
        return opt.isPresent();
    }

    private ModelAndView redirectToCallbackRedirectUrl(final ProfileManager manager,
                                                       final OAuthRegisteredService registeredService,
                                                       final J2EContext context,
                                                       final String clientId) throws Exception {
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            logger.error("Unexpected null profile from profile manager");
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final Service service = createService(registeredService);
        final Authentication authentication = createAuthentication(profile.get(), registeredService, context);

        try {
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, authentication);
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            logger.error(e.getMessage(), e);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final String redirectUri = context.getRequestParameter(OAuthConstants.REDIRECT_URI);
        logger.debug("Authorize request verification successful for client {} with redirect uri {}",
                clientId, redirectUri);

        final String responseType = context.getRequestParameter(OAuthConstants.RESPONSE_TYPE);
        final String callbackUrl;
        if (isResponseType(responseType, OAuthResponseType.CODE)) {
            callbackUrl = buildCallbackUrlForAuthorizationCodeResponseType(authentication, service, redirectUri);
        } else {
            callbackUrl = buildCallbackUrlForImplicitResponseType(context, authentication, service, redirectUri);
        }
        logger.debug("callbackUrl: {}", callbackUrl);
        return OAuthUtils.redirectTo(callbackUrl);
    }

    private String buildCallbackUrlForImplicitResponseType(final J2EContext context,
                                                           final Authentication authentication,
                                                           final Service service,
                                                           final String redirectUri) throws Exception {

        final String state = authentication.getAttributes().get(OAuthConstants.STATE).toString();
        final String nonce = authentication.getAttributes().get(OAuthConstants.NONCE).toString();

        final AccessToken accessToken = generateAccessToken(service, authentication, context);
        logger.debug("Generated Oauth access token: {}", accessToken);

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
        return builder.toString();
    }

    private String buildCallbackUrlForAuthorizationCodeResponseType(final Authentication authentication,
                                                                    final Service service,
                                                                    final String redirectUri) {

        final OAuthCode code = this.oAuthCodeFactory.create(service, authentication);
        logger.debug("Generated OAuth code: {}", code);
        this.ticketRegistry.addTicket(code);

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

        final boolean checkParameterExist = this.validator.checkParameterExist(request, OAuthConstants.CLIENT_ID)
                && this.validator.checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                && this.validator.checkParameterExist(request, OAuthConstants.RESPONSE_TYPE);

        final String responseType = request.getParameter(OAuthConstants.RESPONSE_TYPE);
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

        return checkParameterExist
                && checkResponseTypes(responseType, OAuthResponseType.CODE, OAuthResponseType.TOKEN)
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
    private boolean checkResponseTypes(final String type, final OAuthResponseType... expectedTypes) {
        logger.debug("Response type: {}", type);

        for (final OAuthResponseType expectedType : expectedTypes) {
            if (isResponseType(type, expectedType)) {
                return true;
            }
        }
        logger.error("Unsupported response type: {}", type);
        return false;
    }

    /**
     * Check the response type against an expected response type.
     *
     * @param type         the given response type
     * @param expectedType the expected response type
     * @return whether the response type is the expected one
     */
    private static boolean isResponseType(final String type, final OAuthResponseType expectedType) {
        return expectedType != null && expectedType.name().toLowerCase().equals(type);
    }

    /**
     * Get the OAuth code factory.
     *
     * @return the OAuth code factory
     */
    public OAuthCodeFactory getoAuthCodeFactory() {
        return this.oAuthCodeFactory;
    }

    /**
     * Set the OAuth code factory.
     *
     * @param oAuthCodeFactory the OAuth code factory
     */
    public void setoAuthCodeFactory(final OAuthCodeFactory oAuthCodeFactory) {
        this.oAuthCodeFactory = oAuthCodeFactory;
    }

    public void setConsentApprovalViewResolver(final ConsentApprovalViewResolver consentApprovalViewResolver) {
        this.consentApprovalViewResolver = consentApprovalViewResolver;
    }
}
