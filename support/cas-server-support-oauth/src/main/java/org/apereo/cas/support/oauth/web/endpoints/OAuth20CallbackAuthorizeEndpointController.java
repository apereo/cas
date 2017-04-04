package org.apereo.cas.support.oauth.web.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.springframework.web.CallbackController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuth callback authorize controller based on the pac4j callback controller.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20CallbackAuthorizeEndpointController extends BaseOAuth20Controller {

    private final CallbackController callbackController;
    private final OAuth20CallbackAuthorizeViewResolver oAuth20CallbackAuthorizeViewResolver;

    public OAuth20CallbackAuthorizeEndpointController(final ServicesManager servicesManager,
                                                      final TicketRegistry ticketRegistry,
                                                      final OAuth20Validator validator,
                                                      final AccessTokenFactory accessTokenFactory,
                                                      final PrincipalFactory principalFactory,
                                                      final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                      final Config config,
                                                      final CallbackController callbackController,
                                                      final OAuth20CallbackAuthorizeViewResolver oAuth20CallbackAuthorizeViewResolver,
                                                      final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                      final CasConfigurationProperties casProperties,
                                                      final CookieRetrievingCookieGenerator cookieGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties, cookieGenerator);
        this.callbackController = callbackController;
        this.oAuth20CallbackAuthorizeViewResolver = oAuth20CallbackAuthorizeViewResolver;
        this.callbackController.setConfig(config);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        this.callbackController.callback(request, response);
        final String url = StringUtils.remove(response.getHeader("Location"), "redirect:");
        final J2EContext ctx = WebUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        return oAuth20CallbackAuthorizeViewResolver.resolve(ctx, manager, url);
    }
}
