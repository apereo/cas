package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import lombok.Getter;

/**
 * This is {@link GoogleCaptchaV2Validator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
public class GoogleCaptchaV2Validator extends BaseCaptchaValidator {
    /**
     * Recaptcha response as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_RESPONSE = "g-recaptcha-response";

    private final String recaptchaResponseParameterName = REQUEST_PARAM_RECAPTCHA_RESPONSE;

    public GoogleCaptchaV2Validator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }
}
