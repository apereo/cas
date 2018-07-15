package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20DeviceUserCodeApprovalEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20DeviceUserCodeApprovalEndpointController extends BaseOAuth20Controller {
    public OAuth20DeviceUserCodeApprovalEndpointController(final ServicesManager servicesManager,
                                                           final TicketRegistry ticketRegistry,
                                                           final AccessTokenFactory accessTokenFactory,
                                                           final PrincipalFactory principalFactory,
                                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                           final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                           final CasConfigurationProperties casProperties,
                                                           final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        super(servicesManager, ticketRegistry, accessTokenFactory, principalFactory,
            webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL)
    public ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL)
    public ModelAndView handlePostRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVED_VIEW);
    }
}
