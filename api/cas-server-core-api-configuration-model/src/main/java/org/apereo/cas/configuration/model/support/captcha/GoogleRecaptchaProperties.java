package org.apereo.cas.configuration.model.support.captcha;

import org.apereo.cas.configuration.support.RequiresModule;

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
public class GoogleRecaptchaProperties implements Serializable {

    private static final long serialVersionUID = -8955074129123813915L;

    /**
     * Recaptcha API versions.
     */
    public enum RecaptchaVersions {
        /**
         * V2 version of the recaptcha API.
         */
        V2,
        /**
         * V3 version of the recaptcha API.
         */
        V3
    }

    /**
     * Indicate the version of the recaptcha api.
     * Accepted values are: {@code V2, V3}.
     */
    private RecaptchaVersions version = RecaptchaVersions.V2;

    /**
     * Whether google reCAPTCHA should be enabled.
     */
    private boolean enabled = true;

    /**
     * The google reCAPTCHA site key.
     */
    private String siteKey;

    /**
     * The google reCAPTCHA endpoint for verification of tokens and input.
     */
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * The google reCAPTCHA site secret.
     */
    private String secret;

    /**
     * Whether google reCAPTCHA invisible should be enabled.
     */
    private boolean invisible;

    /**
     * The google reCAPTCHA badge position (only if invisible is enabled).
     * Accepted values are:
     * <ul>
     * <li>{@code bottomright}: default value.</li>
     * <li>{@code bottomleft}</li>
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
    private double score = 0.5;
}
