package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CaptchaValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface CaptchaValidator {
    /**
     * Gets instance.
     *
     * @param googleRecaptcha the google recaptcha
     * @return the instance
     */
    static CaptchaValidator getInstance(final GoogleRecaptchaProperties googleRecaptcha) {
        return switch (googleRecaptcha.getVersion()) {
            case GOOGLE_RECAPTCHA_V2 -> new GoogleCaptchaV2Validator(googleRecaptcha);
            case GOOGLE_RECAPTCHA_V3 -> new GoogleCaptchaV3Validator(googleRecaptcha);
            case HCAPTCHA -> new HCaptchaValidator(googleRecaptcha);
            case TURNSTILE -> new TurnstileCaptchaValidator(googleRecaptcha);
            case FRIENDLY_CAPTCHA -> new FriendlyCaptchaValidator(googleRecaptcha);
        };
    }

    /**
     * Validate.
     *
     * @param recaptchaResponse the recaptcha response
     * @param userAgent         the user agent
     * @return true/false
     */
    boolean validate(String recaptchaResponse, String userAgent);

    /**
     * Gets recaptcha response based on version.
     *
     * @param request the request
     * @return the recaptcha response
     */
    String getRecaptchaResponse(HttpServletRequest request);

    /**
     * Gets recaptcha response parameter name.
     *
     * @return the recaptcha response parameter name
     */
    String getRecaptchaResponseParameterName();

    /**
     * Gets recaptcha properties.
     *
     * @return the recaptcha properties
     */
    GoogleRecaptchaProperties getRecaptchaProperties();
}
