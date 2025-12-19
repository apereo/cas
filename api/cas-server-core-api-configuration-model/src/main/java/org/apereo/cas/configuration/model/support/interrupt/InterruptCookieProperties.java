package org.apereo.cas.configuration.model.support.interrupt;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link InterruptCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class InterruptCookieProperties extends PinnableCookieProperties {
    @Serial
    private static final long serialVersionUID = -266090748600049578L;

    /**
     * Decide if cookie paths should be automatically configured
     * based on the application context path, when the cookie
     * path is not configured.
     */
    private boolean autoConfigureCookiePath = true;

    /**
     * Crypto settings that determine how the cookie should be signed and encrypted.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public InterruptCookieProperties() {
        setName("CASINTERRUPT");
        crypto.setEnabled(true);
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
