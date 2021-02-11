package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link HCaptchaValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class HCaptchaValidator extends BaseCaptchaValidator {
    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_HCAPTCHA_RESPONSE = "h-captcha-response";
    
    public HCaptchaValidator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }

    @Override
    public String getRecaptchaResponse(final HttpServletRequest request) {
        return request.getParameter(REQUEST_PARAM_HCAPTCHA_RESPONSE);
    }
}
