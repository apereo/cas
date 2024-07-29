package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PasswordlessAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-passwordless-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class PasswordlessAuthenticationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 8726382874579042117L;

    /**
     * Properties to instruct CAS how accounts for passwordless authentication should be located.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationAccountsProperties accounts = new PasswordlessAuthenticationAccountsProperties();

    /**
     * Properties to instruct CAS how tokens for passwordless authentication should be located.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationTokensProperties tokens = new PasswordlessAuthenticationTokensProperties();

    /**
     * Core passwordless settings.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationCoreProperties core = new PasswordlessAuthenticationCoreProperties();

    /**
     * Google reCAPTCHA settings.
     */
    @NestedConfigurationProperty
    private GoogleRecaptchaProperties googleRecaptcha = new GoogleRecaptchaProperties();

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(70);
}
