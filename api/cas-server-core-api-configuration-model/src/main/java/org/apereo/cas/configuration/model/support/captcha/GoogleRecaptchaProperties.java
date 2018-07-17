package org.apereo.cas.configuration.model.support.captcha;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

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
public class GoogleRecaptchaProperties implements Serializable {

    private static final long serialVersionUID = -8955074129123813915L;

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
}
