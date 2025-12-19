package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import lombok.Getter;

/**
 * This is {@link TurnstileCaptchaValidator}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
public class TurnstileCaptchaValidator extends BaseCaptchaValidator {
    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_TURNSTILE_RESPONSE = "cf-turnstile-response";

    private final String recaptchaResponseParameterName = REQUEST_PARAM_TURNSTILE_RESPONSE;
    
    public TurnstileCaptchaValidator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }
}
