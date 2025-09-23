package org.apereo.cas.web.flow;

import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
public class ValidateCaptchaAction extends BaseCasWebflowAction {
    private final CaptchaValidator captchaValidator;

    private final CaptchaActivationStrategy captchaActivationStrategy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        if (captchaActivationStrategy.shouldActivate(requestContext, captchaValidator.getRecaptchaProperties()).isEmpty()) {
            LOGGER.debug("Recaptcha is not set to activate for the current request");
            return null;
        }

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val userAgent = WebUtils.getHttpServletRequestUserAgentFromRequestContext(requestContext);
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
