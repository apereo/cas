package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link GoogleCaptchaV2Validator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class GoogleCaptchaV2Validator extends BaseCaptchaValidator {
    /**
     * Recaptcha response as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_RESPONSE = "g-recaptcha-response";

    public GoogleCaptchaV2Validator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }
    
    @Override
    public String getRecaptchaResponse(final HttpServletRequest request) {
        return request.getParameter(REQUEST_PARAM_RECAPTCHA_RESPONSE);
    }
}
