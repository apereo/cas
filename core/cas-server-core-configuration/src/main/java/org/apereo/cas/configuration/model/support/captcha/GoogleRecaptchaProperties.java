package org.apereo.cas.configuration.model.support.captcha;

/**
 * This is {@link GoogleRecaptchaProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleRecaptchaProperties {
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

    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(final String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(final String siteKey) {
        this.siteKey = siteKey;
    }
}
