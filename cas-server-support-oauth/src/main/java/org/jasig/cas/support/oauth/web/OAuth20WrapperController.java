package org.jasig.cas.support.oauth.web;

import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller is the main entry point for OAuth version 2.0
 * wrapping in CAS, should be mapped to something like /oauth2.0/*. Dispatch
 * request to specific controllers : authorize, accessToken...
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("oauth20WrapperController")
public final class OAuth20WrapperController extends BaseOAuthWrapperController {

    @Resource(name="authorizeController")
    private Controller authorizeController;

    @Resource(name="callbackAuthorizeController")
    private Controller callbackAuthorizeController;

    @Resource(name="accessTokenController")
    private Controller accessTokenController;

    @Resource(name="profileController")
    private Controller profileController;

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        // authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method)) {
            return authorizeController.handleRequest(request, response);
        }
        // callback on authorize
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method)) {
            return callbackAuthorizeController.handleRequest(request, response);
        }
        //get access token
        if (OAuthConstants.ACCESS_TOKEN_URL.equals(method)) {
            return accessTokenController.handleRequest(request, response);
        }
        // get profile
        if (OAuthConstants.PROFILE_URL.equals(method)) {
            return profileController.handleRequest(request, response);
        }

        // else error
        logger.error("Unknown method : {}", method);
        OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_OK);
        return null;
    }

    public Controller getAuthorizeController() {
        return authorizeController;
    }

    public Controller getCallbackAuthorizeController() {
        return callbackAuthorizeController;
    }

    public Controller getAccessTokenController() {
        return accessTokenController;
    }

    public Controller getProfileController() {
        return profileController;
    }
}
