package org.apereo.cas.configuration.model.support.captcha;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link GoogleRecaptchaProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-captcha")
@Slf4j
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
}
