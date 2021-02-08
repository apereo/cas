package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties.RecaptchaVersions;

import javax.servlet.http.HttpServletRequest;

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
    static CaptchaValidator getInstance(GoogleRecaptchaProperties googleRecaptcha) {
        if (googleRecaptcha.getVersion() == RecaptchaVersions.GOOGLE_RECAPTCHA_V2) {
            return new GoogleCaptchaV2Validator(googleRecaptcha);
        }
        if (googleRecaptcha.getVersion() == RecaptchaVersions.GOOGLE_RECAPTCHA_V3) {
            return new GoogleCaptchaV3Validator(googleRecaptcha);
        }
        return new HCaptchaValidator(googleRecaptcha);
    }

    /**
     * Validate.
     *
     * @param recaptchaResponse the recaptcha response
     * @param userAgent         the user agent
     * @return the boolean
     */
    boolean validate(String recaptchaResponse, String userAgent);

    /**
     * Gets recaptcha response based on version.
     *
     * @param request the request
     * @return the recaptcha response
     */
    String getRecaptchaResponse(HttpServletRequest request);

}
