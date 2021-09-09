package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link ResetPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
@Getter
@Setter
@JsonFilter("ResetPasswordManagementProperties")
@Accessors(chain = true)
public class ResetPasswordManagementProperties implements Serializable {

    private static final long serialVersionUID = 3453970349530670459L;

    /**
     * Crypto settings on how to reset the password.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

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
     * Whether reset operations require security questions,
     * or should they be marked as optional.
     */
    private boolean securityQuestionsEnabled = true;

    /**
     * Whether the Password Management Token will contain the server IP Address.
     */
    private boolean includeServerIpAddress = true;

    /**
     * Whether the Password Management Token will contain the client IP Address.
     */
    private boolean includeClientIpAddress = true;

    /**
     * How long in minutes should the password expiration link remain valid.
     */
    @DurationCapable
    private String expiration = "PT1M";

    public ResetPasswordManagementProperties() {
        mail.setAttributeName("mail");
        mail.setText("Reset your password via this link: %s");
        sms.setText("Reset your password via this link: %s");
        mail.setSubject("Password Reset");
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
