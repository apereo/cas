package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This controller is in charge of responding to the authorize
 * call in OAuth protocol. It stores the callback url and redirects user to the
 * login page with the callback service.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("authorizeController")
public final class OAuth20AuthorizeController extends BaseOAuthWrapperController {

    /**
     * Instantiates a new o auth20 authorize controller.
     */
    public OAuth20AuthorizeController() {}

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        logger.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        logger.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        final String state = request.getParameter(OAuthConstants.STATE);
        logger.debug("{} : {}", OAuthConstants.STATE, state);

        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            logger.error("Missing {}", OAuthConstants.CLIENT_ID);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            logger.error("Missing {}", OAuthConstants.REDIRECT_URI);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        if (service == null) {
            logger.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final String serviceId = service.getServiceId();
        if (!redirectUri.matches(serviceId)) {
            logger.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        // keep info in session
        final HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.OAUTH20_CALLBACKURL, redirectUri);
        session.setAttribute(OAuthConstants.OAUTH20_SERVICE_NAME, service.getName());
        session.setAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT, service.isBypassApprovalPrompt());
        session.setAttribute(OAuthConstants.OAUTH20_STATE, state);

        final String callbackAuthorizeUrl = request.getRequestURL().toString()
                .replace('/' + OAuthConstants.AUTHORIZE_URL, '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        logger.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_URL, callbackAuthorizeUrl);

        final String loginUrlWithService = OAuthUtils.addParameter(loginUrl, OAuthConstants.SERVICE,
                callbackAuthorizeUrl);
        logger.debug("loginUrlWithService : {}", loginUrlWithService);
        return OAuthUtils.redirectTo(loginUrlWithService);
    }
}
