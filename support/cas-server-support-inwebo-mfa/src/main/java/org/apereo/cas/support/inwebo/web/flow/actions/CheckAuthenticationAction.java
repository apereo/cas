package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.authentication.InweboCredential;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.Result;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to check the push notification or the OTP generated on the browser.
 *
 * @author Jerome LELEU
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class CheckAuthenticationAction extends AbstractAction implements WebflowConstants {

    private final MessageSource messageSource;

    private final InweboService service;

    private final CasWebflowEventResolver casWebflowEventResolver;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val login = authentication.getPrincipal().getId();
        LOGGER.debug("Login: {}", login);
        val otp = requestContext.getRequestParameters().get(OTP);
        val flowScope = requestContext.getFlowScope();
        val sessionId = (String) flowScope.get(INWEBO_SESSION_ID);
        if (StringUtils.isNotBlank(otp)) {
            val response = service.authenticateExtended(login, otp);
            if (response.isOk()) {
                val deviceName = response.getDeviceName();
                LOGGER.info("User: {} validated OTP on device: {}", login, deviceName);
                WebUtils.putCredential(requestContext, new InweboCredential(login, deviceName));
                return this.casWebflowEventResolver.resolveSingle(requestContext);
            }
        } else if (StringUtils.isNotBlank(sessionId)) {
            val response = service.checkPushResult(login, sessionId);
            val result = response.getResult();
            if (response.isOk()) {
                val deviceName = response.getDeviceName();
                LOGGER.info("User: {} validated push on device: {}", login, deviceName);
                WebUtils.putCredential(requestContext, new InweboCredential(login, deviceName));
                return this.casWebflowEventResolver.resolveSingle(requestContext);
            } else if (result == Result.WAITING) {
                LOGGER.debug("Waiting for user to validate on mobile/desktop");
                return getEventFactorySupport().event(this, PENDING);
            } else {
                LOGGER.debug("Validation fails: {}", result);
                if (result == Result.REFUSED || result == Result.TIMEOUT) {
                    flowScope.put(INWEBO_ERROR_MESSAGE,
                            messageSource.getMessage("cas.inwebo.error.userrefusedortoolate", null, LocaleContextHolder.getLocale()));
                }
            }
        }
        return error();
    }
}
