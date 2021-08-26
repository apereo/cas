package org.apereo.cas.configuration.model.support.captcha;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link GoogleRecaptchaProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-captcha")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GoogleRecaptchaProperties")
public class GoogleRecaptchaProperties implements Serializable {

    private static final long serialVersionUID = -8955074129123813915L;

    /**
     * Recaptcha API versions.
     */
    public enum RecaptchaVersions {
        /**
         * V2 version of the recaptcha API.
         */
        GOOGLE_RECAPTCHA_V2,
        /**
         * V3 version of the recaptcha API.
         */
        GOOGLE_RECAPTCHA_V3,
        /**
         * hCaptcha.
         */
        HCAPTCHA
    }

    /**
     * Indicate the version of the recaptcha api.
     * Accepted values are: {@code V2, V3}.
     */
    private RecaptchaVersions version = RecaptchaVersions.GOOGLE_RECAPTCHA_V2;

    /**
     * Whether google reCAPTCHA should be enabled.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * The google reCAPTCHA site key.
     */
    @RequiredProperty
    private String siteKey;

    /**
     * The google reCAPTCHA endpoint for verification of tokens and input.
     */
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * The google reCAPTCHA site secret.
     */
    @RequiredProperty
    private String secret;

    /**
     * Whether google reCAPTCHA invisible should be enabled.
     */
    private boolean invisible;

    /**
     * The google reCAPTCHA badge position (only if invisible is enabled).
     * Accepted values are:
     * <ul>
     * <li>{@code bottomright}: bottom right corner, default value.</li>
     * <li>{@code bottomleft}: bottom left corner</li>
     * <li>{@code inline}: allows to control the CSS.</li>
     * </ul>
     */
    private String position = "bottomright";

    /**
     * reCAPTCHA v3 returns a score (1.0 is very likely a good interaction, 0.0 is very likely a bot).
     * reCAPTCHA learns by seeing real traffic on your site. For this reason, scores in a staging
     * environment or soon after implementing may differ from production. As reCAPTCHA v3 doesn't
     * ever interrupt the user flow, you can first run reCAPTCHA without taking action and then
     * decide on thresholds by looking at your traffic in the admin console.
     * By default, you can use a threshold of 0.5.
     */
    @RequiredProperty
    private double score = 0.5;
}
