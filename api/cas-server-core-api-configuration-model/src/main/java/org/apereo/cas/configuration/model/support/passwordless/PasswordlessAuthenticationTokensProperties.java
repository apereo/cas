package org.apereo.cas.configuration.model.support.passwordless;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationJpaTokensProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationMongoDbTokensProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationRestTokensProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationTokensCoreProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link PasswordlessAuthenticationTokensProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-passwordless-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationTokensProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8371063350377031703L;

    /**
     * Core settings on passwordless authn.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationTokensCoreProperties core = new PasswordlessAuthenticationTokensCoreProperties();

    /**
     * Crypto settings for passwordless authn.
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
     * Passwordless authentication settings via MongoDb.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationMongoDbTokensProperties mongo = new PasswordlessAuthenticationMongoDbTokensProperties();

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
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
