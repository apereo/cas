package org.apereo.cas.configuration.model.support.account;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;

import java.io.Serializable;

/**
 * This is {@link AccountManagementRegistrationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-account-mgmt")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AccountManagementRegistrationCoreProperties")
public class AccountManagementRegistrationCoreProperties implements Serializable {
    private static final long serialVersionUID = -4679683905941523034L;

    /**
     * Registration properties.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties registrationProperties = new SpringResourceProperties();

    /**
     * Whether the registration token will contain the server IP Address.
     */
    private boolean includeServerIpAddress = true;

    /**
     * Whether the registration token will contain the client IP Address.
     */
    private boolean includeClientIpAddress = true;

    /**
     * How long in minutes should the registration link remain valid.
     */
    private long expirationMinutes = 3;

    /**
     * Crypto settings on how to generate registration requests.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public AccountManagementRegistrationCoreProperties() {
        val resource = new ClassPathResource("account-registration-properties/registration-properties.json");
        registrationProperties.setLocation(resource);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
