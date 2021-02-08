package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link GoogleCaptchaV3Validator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class GoogleCaptchaV3Validator extends BaseCaptchaValidator {

    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_TOKEN = "g-recaptcha-token";

    public GoogleCaptchaV3Validator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }

    @Override
    public String getRecaptchaResponse(final HttpServletRequest request) {
        return request.getParameter(REQUEST_PARAM_RECAPTCHA_TOKEN);
    }
}
