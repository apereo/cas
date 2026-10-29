package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    /**
     * Parameter name of the approval token that the approval form carries, so that only the form CAS rendered for
     * the signed-in user can approve a device user code.
     */
    public static final String PARAMETER_APPROVAL_TOKEN = "approvaltoken";

    /**
     * Model attribute that holds the approval token for the form.
     */
    public static final String MODEL_ATTRIBUTE_APPROVAL_TOKEN = "approvalToken";

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
        val ticketGrantingTicket = fetchTicketGrantingTicket(request, response);
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, getApprovalModel(StringUtils.EMPTY, ticketGrantingTicket));
    }

    /**
     * Handle request internal model and view.
     * The request must carry the approval token of the form CAS rendered for the single sign-on session;
     * a request without it, such as one another site makes the browser send, does not approve anything.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL)
    @Operation(summary = "Handle device code approval request")
    public ModelAndView handlePostRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val ticketGrantingTicket = fetchTicketGrantingTicket(request, response);
        if (ticketGrantingTicket == null || !isApprovalTokenValid(request, ticketGrantingTicket)) {
            LOGGER.warn("Device user code approval request does not carry a valid approval token and is refused");
            return getModelAndViewForFailure("invalidrequest", ticketGrantingTicket);
        }
        val userCode = request.getParameter(PARAMETER_USER_CODE);
        if (StringUtils.isBlank(userCode)) {
            return getModelAndViewForFailure("codenotfound", ticketGrantingTicket);
        }
        try {
            val factory = (OAuth20DeviceUserCodeFactory) getConfigurationContext().getTicketFactory().get(OAuth20DeviceUserCode.class);
            val codeId = factory.normalizeUserCode(userCode);
            val deviceUserCode = getConfigurationContext().getTicketRegistry().getTicket(codeId, OAuth20DeviceUserCode.class);
            if (deviceUserCode.isUserCodeApproved()) {
                return getModelAndViewForFailure("codeapproved", ticketGrantingTicket);
            }
            LOGGER.debug("Approved device user code: [{}] for [{}]", deviceUserCode.getId(),
                ticketGrantingTicket.getAuthentication().getPrincipal().getId());
            deviceUserCode.setAuthentication(ticketGrantingTicket.getAuthentication());
            deviceUserCode.setUserCodeApproved(true);

            val updatedDeviceUserCode = getConfigurationContext().getTicketRegistry().updateTicket(deviceUserCode);
            return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVED_VIEW,
                Map.of("code", updatedDeviceUserCode.getId()), HttpStatus.OK);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return getModelAndViewForFailure("codenotfound", ticketGrantingTicket);
        }
    }

    /**
     * Build the approval token for the single sign-on session. The token is an HMAC of the ticket-granting ticket id,
     * keyed by the ticket-granting cookie keys when they are defined, and by the ticket-granting ticket id otherwise,
     * so it can only be produced by CAS or by someone who already holds the session, and it holds on every node.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the approval token
     */
    protected String buildApprovalToken(final TicketGrantingTicket ticketGrantingTicket) {
        val crypto = getConfigurationContext().getCasProperties().getTgc().getCrypto();
        val secretKey = StringUtils.defaultIfBlank(
            StringUtils.join(crypto.getSigning().getKey(), crypto.getEncryption().getKey()), ticketGrantingTicket.getId());
        return DigestUtils.sha512Hmac(secretKey, OAuth20DeviceUserCode.PREFIX + '/' + ticketGrantingTicket.getId());
    }

    private boolean isApprovalTokenValid(final HttpServletRequest request, final TicketGrantingTicket ticketGrantingTicket) {
        val approvalToken = request.getParameter(PARAMETER_APPROVAL_TOKEN);
        return StringUtils.isNotBlank(approvalToken) && MessageDigest.isEqual(
            buildApprovalToken(ticketGrantingTicket).getBytes(StandardCharsets.UTF_8), approvalToken.getBytes(StandardCharsets.UTF_8));
    }

    private @Nullable TicketGrantingTicket fetchTicketGrantingTicket(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            return getConfigurationContext().fetchTicketGrantingTicketFrom(request, response);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return null;
        }
    }

    private ModelAndView getModelAndViewForFailure(final String code, final @Nullable TicketGrantingTicket ticketGrantingTicket) {
        return new ModelAndView(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, getApprovalModel(code, ticketGrantingTicket));
    }

    private Map<String, Object> getApprovalModel(final String errorCode, final @Nullable TicketGrantingTicket ticketGrantingTicket) {
        val map = new LinkedHashMap<String, Object>();
        map.put("prefix", OAuth20DeviceUserCode.PREFIX);
        if (StringUtils.isNotBlank(errorCode)) {
            map.put("error", errorCode);
        }
        if (ticketGrantingTicket != null) {
            map.put(MODEL_ATTRIBUTE_APPROVAL_TOKEN, buildApprovalToken(ticketGrantingTicket));
        }
        return map;
    }
}
