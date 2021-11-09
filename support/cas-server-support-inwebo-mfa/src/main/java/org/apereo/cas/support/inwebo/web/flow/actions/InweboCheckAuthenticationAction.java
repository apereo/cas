package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.authentication.InweboCredential;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to check the push notification or the OTP generated on the browser.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class InweboCheckAuthenticationAction extends AbstractAction {

    private final InweboService service;

    private final CasWebflowEventResolver casWebflowEventResolver;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val login = authentication.getPrincipal().getId();
        val otp = requestContext.getRequestParameters().get(WebflowConstants.OTP);
        val flowScope = requestContext.getFlowScope();
        val sessionId = (String) flowScope.get(WebflowConstants.INWEBO_SESSION_ID);
        if (StringUtils.isNotBlank(otp)) {
            val credential = new InweboCredential(login);
            credential.setOtp(otp);
            LOGGER.debug("Received OTP: [{}] for login: [{}]", otp, login);
            WebUtils.putCredential(requestContext, credential);
            return this.casWebflowEventResolver.resolveSingle(requestContext);
        }
        if (StringUtils.isNotBlank(sessionId)) {
            val response = service.checkPushResult(login, sessionId);
            val result = response.getResult();
            if (response.isOk()) {
                val deviceName = response.getDeviceName();
                val credential = new InweboCredential(login);
                credential.setDeviceName(deviceName);
                credential.setAlreadyAuthenticated(true);
                LOGGER.debug("User: [{}] validated push for sessionId: [{}] and device: [{}]", login, sessionId, deviceName);
                WebUtils.putCredential(requestContext, credential);
                return this.casWebflowEventResolver.resolveSingle(requestContext);
            }
            if (result == InweboResult.WAITING) {
                LOGGER.trace("Waiting for user to validate on mobile/desktop");
                return getEventFactorySupport().event(this, WebflowConstants.PENDING);
            }
            LOGGER.debug("Validation fails: [{}]", result);
            if (result == InweboResult.REFUSED || result == InweboResult.TIMEOUT) {
                WebUtils.addErrorMessageToContext(requestContext, "cas.inwebo.error.userrefusedortoolate");
            }
        }
        return error();
    }

}
