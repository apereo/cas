package org.jasig.cas.support.oauth.web;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.PrincipalException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredServiceAccessStrategyUtils;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.support.oauth.ticket.code.OAuthCodeFactory;
import org.jasig.cas.support.oauth.util.OAuthUtils;
import org.jasig.cas.util.EncodingUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller is in charge of responding to the authorize call in OAuth v2 protocol.
 * This url is protected by a CAS authentication. It returns an OAuth code or directly an access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RefreshScope
@Component("authorizeController")
@Controller
public class OAuth20AuthorizeController extends BaseOAuthWrapperController {
    
    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory oAuthCodeFactory;

    @RequestMapping(path=OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.AUTHORIZE_URL)
    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        if (!verifyAuthorizeRequest(request)) {
            logger.error("Authorize request verification fails");
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        final String state = request.getParameter(OAuthConstants.STATE);
        final String bypassApprovalParameter = request.getParameter(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        logger.debug("bypassApprovalParameter: {}", bypassApprovalParameter);

        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientId, registeredService);
        } catch (final UnauthorizedServiceException e) {
            logger.error(e.getMessage(), e);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        
        final boolean bypassApprovalService = registeredService.isBypassApprovalPrompt();
        logger.debug("bypassApprovalService: {}", bypassApprovalService);

        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final UserProfile profile = manager.get(true);
        
        if (profile == null) {
            logger.error("Unexpected null profile");
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        // bypass approval -> redirect to the application with code or access token
        if (bypassApprovalService || bypassApprovalParameter != null) {
            final Service service = createService(registeredService);
            final Authentication authentication = createAuthentication(profile, registeredService);

            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, 
                        registeredService, authentication);
            } catch (final UnauthorizedServiceException | PrincipalException e) {
                logger.error(e.getMessage(), e);
                return new ModelAndView(OAuthConstants.ERROR_VIEW);
            }

            final String responseType = request.getParameter(OAuthConstants.RESPONSE_TYPE);
            final String callbackUrl;
            if (isResponseType(responseType, OAuthResponseType.CODE)) {
                callbackUrl = buildCallbackUrlForAuthorizationCodeResponseType(state, authentication, service, redirectUri);
            } else {
                callbackUrl = buildCallbackUrlForImplicitResponseType(state, authentication, service, redirectUri);
            }
            logger.debug("callbackUrl: {}", callbackUrl);
            return OAuthUtils.redirectTo(callbackUrl);
        }

        return redirectToApproveView(registeredService, context);
    }

    private String buildCallbackUrlForImplicitResponseType(final String state, final Authentication authentication, 
                                                             final Service service, final String redirectUri) {
        final AccessToken accessToken = generateAccessToken(service, authentication);
        logger.debug("Generated Oauth access token: {}", accessToken);

        String callbackUrl = redirectUri;
        callbackUrl += "#access_token=" + accessToken.getId() + "&token_type=bearer&expires_in=" + this.timeout;
        if (state != null) {
            callbackUrl += "&state=" + EncodingUtils.urlEncode(state);
        }
        return callbackUrl;
    }

    private String buildCallbackUrlForAuthorizationCodeResponseType(final String state, final Authentication authentication,
                                                                    final Service service, final String redirectUri) {
        final OAuthCode code = this.oAuthCodeFactory.create(service, authentication);
        logger.debug("Generated OAuth code: {}", code);
        this.ticketRegistry.addTicket(code);

        String callbackUrl = redirectUri;
        callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.CODE, code.getId());
        if (state != null) {
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.STATE, state);
        }
        return callbackUrl;
    }

    private ModelAndView redirectToApproveView(final OAuthRegisteredService registeredService, final J2EContext context) {
        String callbackUrl = context.getFullRequestURL();
        callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.BYPASS_APPROVAL_PROMPT, "true");
        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", callbackUrl);
        model.put("serviceName", registeredService.getName());
        logger.debug("callbackUrl: {}", callbackUrl);
        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
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
     * @param type the current response type
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
     * @param type the given response type
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
}
