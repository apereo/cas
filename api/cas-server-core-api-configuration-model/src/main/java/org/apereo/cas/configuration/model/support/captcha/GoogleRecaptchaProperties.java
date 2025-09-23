package org.apereo.cas.configuration.model.support.captcha;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
public class GoogleRecaptchaProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -8955074129123813915L;

    /**
     * Indicate the version of the recaptcha api.
     */
    private RecaptchaVersions version = RecaptchaVersions.GOOGLE_RECAPTCHA_V2;

    /**
     * Whether reCAPTCHA should be enabled.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * The reCAPTCHA site key.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String siteKey;

    /**
     * The reCAPTCHA endpoint for verification of the reCAPTCHA response.
     * The endpoint is specific to the reCAPTCHA provider:
     * <ul>
     *     <li>For Google reCAPTCHA, the endpoint is {@code https://www.google.com/recaptcha/api/siteverify}.</li>
     *     <li>For hCaptcha, the endpoint is {@code https://hcaptcha.com/siteverify}.</li>
     *     <li>For Turnstile, the endpoint is {@code https://challenges.cloudflare.com/turnstile/v0/siteverify}.</li>
     * </ul>
     */
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * The reCAPTCHA site secret.
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
     * <li>{@code inline}: allows one to control the CSS.</li>
     * </ul>
     */
    private String position = "bottomright";

    /**
     * Google reCAPTCHA v3 returns a score (1.0 is very likely a good interaction, 0.0 is very likely a bot).
     * reCAPTCHA learns by seeing real traffic on your site. For this reason, scores in a staging
     * environment or soon after implementing may differ from production. As reCAPTCHA v3 doesn't
     * ever interrupt the user flow, you can first run reCAPTCHA without taking action and then
     * decide on thresholds by looking at your traffic in the admin console.
     * By default, you can use a threshold of 0.5.
     */
    @RequiredProperty
    private double score = 0.5;

    /**
     * A regular expression pattern to indicate that
     * captcha should be activated when the remote IP address
     * matches this pattern, and otherwise skipped and disabled.
     */
    @RegularExpressionCapable
    private String activateForIpAddressPattern;

    /**
     * Headers, defined as a {@link Map}, to include in the request when making
     * the verification call to the recaptcha API.
     */
    private Map<String, String> headers = new HashMap<>();
    
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
         * hCaptcha is an alternative to reCAPTCHA that requires website visitors
         * to label images as part of its business model.
         * The provider is more focused on manual image recognition challenges. It is a image classification
         * task based CAPTCHA provider employing visual challenges like identifying objects.
         */
        HCAPTCHA,
        /**
         * Offers a CAPTCHA alternative by Cloudflare that prioritizes user experience and privacy.
         * Turnstile delivers frustration-free, CAPTCHA-free web experiences to website visitors -
         * with just a simple snippet of free code. Moreover, Turnstile stops abuse and confirms
         * visitors are real without the data privacy concerns or awful user experience of CAPTCHAs.
         */
        TURNSTILE,
        /**
         * Friendly captcha is a European alternative, accessible and GDPR compliant.
         */
        FRIENDLY_CAPTCHA
    }
}
