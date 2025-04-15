package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
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

@Accessors(chain = true)
public class ResetPasswordManagementProperties implements CasFeatureModule, Serializable {

    @Serial
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
     * Email settings for password reset confirmations.
     */
    @NestedConfigurationProperty
    private EmailProperties confirmationMail = new EmailProperties();

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
     * Controls whether password reset operations must activate
     * and support a multifactor authentication flow based on the
     * set of available MFA providers that are configured and active, before reset
     * instructions can be shared and sent.
     */
    private boolean multifactorAuthenticationEnabled = true;

    /**
     * How long in minutes should the password expiration link remain valid.
     */
    @DurationCapable
    private String expiration = "PT1M";

    /**
     * How many times you can use the password reset link.
     * Strictly lower than 1 means infinite.
     */
    private int numberOfUses = -1;

    public ResetPasswordManagementProperties() {
        mail.setText("Reset your password via this link: ${url}");
        confirmationMail.setText("Your password reset request has been confirmed");
        sms.setText("Reset your password via this link: ${url}");

        mail.setSubject("Password Reset");
        confirmationMail.setSubject("Password Reset Confirmation");

        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
