package org.apereo.cas.configuration.model.support.account;

import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.model.support.account.provision.AccountManagementRegistrationProvisioningProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link AccountManagementRegistrationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-account-mgmt")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AccountManagementRegistrationProperties")
public class AccountManagementRegistrationProperties implements Serializable {
    private static final long serialVersionUID = -4679683905941523034L;

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(100);

    /**
     * Google reCAPTCHA settings.
     */
    @NestedConfigurationProperty
    private GoogleRecaptchaProperties googleRecaptcha = new GoogleRecaptchaProperties();

    /**
     * Core settings.
     */
    @NestedConfigurationProperty
    private AccountManagementRegistrationCoreProperties core = new AccountManagementRegistrationCoreProperties();

    /**
     * Provisioning settings.
     */
    @NestedConfigurationProperty
    private AccountManagementRegistrationProvisioningProperties provisioning = new AccountManagementRegistrationProvisioningProperties();

    public AccountManagementRegistrationProperties() {
        mail.setAttributeName("mail");
        mail.setText("Activate your account registration via this link: %s");
        sms.setText("Activate your account registration via this link: %s");
        mail.setSubject("Account Registration");
    }
}
