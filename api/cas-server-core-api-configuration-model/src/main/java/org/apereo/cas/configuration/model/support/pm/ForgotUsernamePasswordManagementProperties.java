package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
public class ForgotUsernamePasswordManagementProperties implements Serializable {
    private static final long serialVersionUID = 4850199066765183587L;

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
        this.mail.setAttributeName("mail");
        this.mail.setText("Your current username is: %s");
        this.mail.setSubject("Forgot Username");
    }
}

