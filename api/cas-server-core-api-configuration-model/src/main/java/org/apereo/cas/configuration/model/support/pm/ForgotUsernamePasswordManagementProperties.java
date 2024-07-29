package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ForgotUsernamePasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class ForgotUsernamePasswordManagementProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 4850199066765183587L;

    /**
     * Whether forgot/reset username functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * Google reCAPTCHA settings.
     */
    @NestedConfigurationProperty
    private GoogleRecaptchaProperties googleRecaptcha = new GoogleRecaptchaProperties();

    public ForgotUsernamePasswordManagementProperties() {
        this.mail.setText("Your current username is: ${username}");
        this.mail.setSubject("Forgot Username");
    }
}

