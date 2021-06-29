package org.apereo.cas.web.flow;

import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ValidateCaptchaAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ValidateCaptchaAction extends AbstractAction {
    private final CaptchaValidator captchaValidator;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val userAgent = WebUtils.getHttpServletRequestUserAgentFromRequestContext();

        val gRecaptchaResponse = captchaValidator.getRecaptchaResponse(request);
        if (StringUtils.isBlank(gRecaptchaResponse)) {
            LOGGER.warn("Recaptcha response/token is missing from the request");
            return getError(requestContext);
        }
        val result = captchaValidator.validate(gRecaptchaResponse, userAgent);
        if (result) {
            LOGGER.debug("Recaptcha has successfully validated the request");
            return null;
        }
        return getError(requestContext);
    }

    private Event getError(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR,
            CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR);
        return getEventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR);
    }
}
