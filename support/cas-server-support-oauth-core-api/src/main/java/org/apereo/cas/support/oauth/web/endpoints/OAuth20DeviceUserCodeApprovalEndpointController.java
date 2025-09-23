package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.util.LoggingUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20DeviceUserCodeApprovalEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Tag(name = "OAuth")
public class OAuth20DeviceUserCodeApprovalEndpointController extends BaseOAuth20Controller<OAuth20ConfigurationContext> {
    /**
     * User code parameter name.
     */
    public static final String PARAMETER_USER_CODE = "usercode";

    public OAuth20DeviceUserCodeApprovalEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL)
    @Operation(summary = "Handle device code approval request")
    public ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val model = getApprovalModel(StringUtils.EMPTY);
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, model);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL)
    @Operation(summary = "Handle device code approval request")
    public ModelAndView handlePostRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val userCode = request.getParameter(PARAMETER_USER_CODE);
        val codeNotfound = getModelAndViewForFailure("codenotfound");
        if (StringUtils.isBlank(userCode)) {
            return codeNotfound;
        }
        try {
            val factory = (OAuth20DeviceUserCodeFactory) getConfigurationContext().getTicketFactory().get(OAuth20DeviceUserCode.class);
            val codeId = factory.normalizeUserCode(userCode);
            val deviceUserCode = getConfigurationContext().getTicketRegistry().getTicket(codeId, OAuth20DeviceUserCode.class);
            if (deviceUserCode.isUserCodeApproved()) {
                return getModelAndViewForFailure("codeapproved");
            }
            deviceUserCode.setUserCodeApproved(true);
            val updatedDeviceUserCode = getConfigurationContext().getTicketRegistry().updateTicket(deviceUserCode);
            return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVED_VIEW,
                Map.of("code", updatedDeviceUserCode.getId()), HttpStatus.OK);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return codeNotfound;
        }
    }

    private static ModelAndView getModelAndViewForFailure(final String code) {
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, getApprovalModel(code));
    }

    private static Map getApprovalModel(final String errorCode) {
        val map = new LinkedHashMap<String, Object>();
        map.put("prefix", OAuth20DeviceUserCode.PREFIX);
        if (StringUtils.isNotBlank(errorCode)) {
            map.put("error", errorCode);
        }
        return map;
    }
}
