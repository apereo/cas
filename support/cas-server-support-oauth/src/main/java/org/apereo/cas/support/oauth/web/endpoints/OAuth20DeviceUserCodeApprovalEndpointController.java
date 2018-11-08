package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.device.DeviceTokenFactory;
import org.apereo.cas.ticket.device.DeviceUserCode;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20DeviceUserCodeApprovalEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20DeviceUserCodeApprovalEndpointController extends BaseOAuth20Controller {
    /**
     * User code parameter name.
     */
    public static final String PARAMETER_USER_CODE = "usercode";

    private final DeviceTokenFactory deviceTokenFactory;

    public OAuth20DeviceUserCodeApprovalEndpointController(final ServicesManager servicesManager,
                                                           final TicketRegistry ticketRegistry,
                                                           final AccessTokenFactory accessTokenFactory,
                                                           final PrincipalFactory principalFactory,
                                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                           final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                           final CasConfigurationProperties casProperties,
                                                           final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                           final DeviceTokenFactory deviceTokenFactory) {
        super(servicesManager, ticketRegistry, accessTokenFactory, principalFactory,
            webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
        this.deviceTokenFactory = deviceTokenFactory;
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
    public static ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val model = getApprovalModel(StringUtils.EMPTY);
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, model);
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
        val userCode = request.getParameter(PARAMETER_USER_CODE);
        val codeNotfound = getModelAndViewForFailure("codenotfound");
        if (StringUtils.isBlank(userCode)) {
            return codeNotfound;
        }
        val codeId = deviceTokenFactory.generateDeviceUserCode(userCode);
        val deviceUserCode = this.ticketRegistry.getTicket(codeId, DeviceUserCode.class);
        if (deviceUserCode == null) {
            return codeNotfound;
        }
        if (deviceUserCode.isExpired()) {
            return getModelAndViewForFailure("codeexpired");
        }
        if (deviceUserCode.isUserCodeApproved()) {
            return getModelAndViewForFailure("codeapproved");
        }
        deviceUserCode.approveUserCode();
        this.ticketRegistry.updateTicket(deviceUserCode);
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVED_VIEW, HttpStatus.OK);
    }

    private static ModelAndView getModelAndViewForFailure(final String code) {
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, getApprovalModel(code));
    }

    private static Map getApprovalModel(final String errorCode) {
        val map = new LinkedHashMap<>();
        map.put("prefix", DeviceUserCode.PREFIX);
        if (StringUtils.isNotBlank(errorCode)) {
            map.put("error", errorCode);
        }
        return map;
    }
}
