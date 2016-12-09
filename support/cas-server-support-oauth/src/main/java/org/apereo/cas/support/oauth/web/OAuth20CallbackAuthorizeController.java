package org.apereo.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.springframework.web.CallbackController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuth callback authorize controller based on the pac4j callback controller.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20CallbackAuthorizeController extends BaseOAuthWrapperController {

    private Config config;

    private CallbackController callbackController;

    private OAuth20CallbackAuthorizeViewResolver oAuth20CallbackAuthorizeViewResolver;

    public OAuth20CallbackAuthorizeController(final ServicesManager servicesManager,
                                              final TicketRegistry ticketRegistry,
                                              final OAuth20Validator validator,
                                              final AccessTokenFactory accessTokenFactory,
                                              final PrincipalFactory principalFactory,
                                              final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                              final Config config,
                                              final CallbackController callbackController,
                                              final OAuth20CallbackAuthorizeViewResolver oAuth20CallbackAuthorizeViewResolver) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory, webApplicationServiceServiceFactory);
        this.config = config;
        this.callbackController = callbackController;
        this.oAuth20CallbackAuthorizeViewResolver = oAuth20CallbackAuthorizeViewResolver;
    }

    @PostConstruct
    private void postConstruct() {
        this.callbackController.setConfig(this.config);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL)
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        this.callbackController.callback(request, response);
        final String url = StringUtils.remove(response.getHeader("Location"), "redirect:");
        final J2EContext ctx = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(ctx);
        return oAuth20CallbackAuthorizeViewResolver.resolve(ctx, manager, url);
    }
}
