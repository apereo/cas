package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import lombok.Getter;

/**
 * This is {@link HCaptchaValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
public class HCaptchaValidator extends BaseCaptchaValidator {
    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_HCAPTCHA_RESPONSE = "h-captcha-response";

    private final String recaptchaResponseParameterName = REQUEST_PARAM_HCAPTCHA_RESPONSE;
    
    public HCaptchaValidator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }
}
