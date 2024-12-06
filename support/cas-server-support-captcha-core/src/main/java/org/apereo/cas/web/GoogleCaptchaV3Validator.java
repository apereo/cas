package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import lombok.Getter;

/**
 * This is {@link GoogleCaptchaV3Validator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
public class GoogleCaptchaV3Validator extends BaseCaptchaValidator {

    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_TOKEN = "g-recaptcha-token";

    private final String recaptchaResponseParameterName = REQUEST_PARAM_RECAPTCHA_TOKEN;
    
    public GoogleCaptchaV3Validator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }
}
