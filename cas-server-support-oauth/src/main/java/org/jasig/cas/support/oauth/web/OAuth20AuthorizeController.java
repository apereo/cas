package org.jasig.cas.support.oauth.web;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.authentication.OAuthAuthentication;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.services.OAuthWebApplicationService;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.support.oauth.ticket.code.OAuthCodeFactory;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller is in charge of responding to the authorize call in OAuth v2 protocol.
 * This url is protected by a CAS authentication.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("authorizeController")
public final class OAuth20AuthorizeController extends BaseOAuthWrapperController {

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier("defaultPrincipalFactory")
    private PrincipalFactory principalFactory;

    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        if (!verifyAuthorizeRequest(request)) {
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        final String state = request.getParameter(OAuthConstants.STATE);
        final String bypassApprovalParameter = request.getParameter(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        logger.debug("bypassApprovalParameter: {}", bypassApprovalParameter);

        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        final boolean bypassApprovalService = registeredService.isBypassApprovalPrompt();
        logger.debug("bypassApprovalService: {}", bypassApprovalService);

        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final UserProfile profile = manager.get(true);
        CommonHelper.assertNotNull("profile", profile);

        // bypass approval -> redirect to the application with code
        if (bypassApprovalService || bypassApprovalParameter != null) {
            final Principal principal = principalFactory.createPrincipal(profile.getId(), profile.getAttributes());
            final Authentication authentication = new OAuthAuthentication(ZonedDateTime.now(), principal);
            final Service service = new OAuthWebApplicationService("" + registeredService.getId(), registeredService.getServiceId());
            final OAuthCode code = oAuthCodeFactory.create(service, authentication);
            logger.debug("Generated OAuth code: {}", code);
            ticketRegistry.addTicket(code);

            String callbackUrl = redirectUri;
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.CODE, code.getId());
            if (state != null) {
                callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.STATE, state);
            }
            logger.debug("callbackUrl: {}", callbackUrl);
            return OAuthUtils.redirectTo(callbackUrl);
        } else {
            // redirect to approval screen
            String callbackUrl = context.getFullRequestURL();
            callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.BYPASS_APPROVAL_PROMPT, "true");
            final Map<String, Object> model = new HashMap<>();
            model.put("callbackUrl", callbackUrl);
            model.put("serviceName", registeredService.getName());
            logger.debug("callbackUrl: {}", callbackUrl);
            return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
        }
    }

    /**
     * Verify the authorize request.
     *
     * @param request the HTTP request
     * @return whether the authorize request is valid
     */
    private boolean verifyAuthorizeRequest(final HttpServletRequest request) {

        return checkParameterExist(request, OAuthConstants.CLIENT_ID)
            && checkParameterExist(request, OAuthConstants.REDIRECT_URI)
            && checkServiceValid(request)
            && checkCallbackValid(request);
    }

    /**
     * Get the OAuth code factory.
     *
     * @return the OAuth code factory.
     */
    public OAuthCodeFactory getoAuthCodeFactory() {
        return oAuthCodeFactory;
    }

    /**
     * Set the OAuth code factory.
     *
     * @param oAuthCodeFactory the OAuth code factory.
     */
    public void setoAuthCodeFactory(final OAuthCodeFactory oAuthCodeFactory) {
        this.oAuthCodeFactory = oAuthCodeFactory;
    }

    public PrincipalFactory getPrincipalFactory() {
        return principalFactory;
    }

    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
