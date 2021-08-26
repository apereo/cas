package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationJpaTokensProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationRestTokensProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link PasswordlessAuthenticationTokensProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-passwordless")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PasswordlessAuthenticationTokensProperties")
public class PasswordlessAuthenticationTokensProperties implements Serializable {

    private static final long serialVersionUID = 8371063350377031703L;

    /**
     * Indicate how long should the token be considered valid.
     */
    private int expireInSeconds = 180;

    /**
     * Crypto settings on how to reset the password.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * Passwordless authentication settings via REST.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationRestTokensProperties rest = new PasswordlessAuthenticationRestTokensProperties();

    /**
     * Passwordless authentication settings via JPA.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationJpaTokensProperties jpa = new PasswordlessAuthenticationJpaTokensProperties();

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

    public PasswordlessAuthenticationTokensProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
