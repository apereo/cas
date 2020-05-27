package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
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
    private final GoogleRecaptchaProperties recaptchaProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val userAgent = WebUtils.getHttpServletRequestUserAgentFromRequestContext();

        val gRecaptchaResponse = CaptchaValidator.getRecaptchaResponse(recaptchaProperties.getVersion(), request);
        if (StringUtils.isBlank(gRecaptchaResponse)) {
            LOGGER.warn("Recaptcha response/token is missing from the request");
            return getError(requestContext);
        }

        val validator = new CaptchaValidator(recaptchaProperties.getVerifyUrl(), recaptchaProperties.getSecret(), recaptchaProperties.getScore());
        val result = validator.validate(gRecaptchaResponse, userAgent);
        if (result) {
            LOGGER.debug("Recaptcha has successfully validated the request");
            return null;
        }
        return getError(requestContext);
    }

    private Event getError(final RequestContext requestContext) {
        val messageContext = requestContext.getMessageContext();
        messageContext.addMessage(new MessageBuilder()
            .error()
            .code(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR)
            .defaultText(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR)
            .build());
        return getEventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR);
    }
}
