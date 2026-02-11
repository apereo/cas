package org.apereo.cas.configuration.model.support.wsfed;

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
 * This is {@link WsFederationDelegatedCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated Since 8.0.0, WS-Federation delegation support is deprecated and scheduled for removal.
 */
@RequiresModule(name = "cas-server-support-wsfederation-webflow")
@Getter
@Accessors(chain = true)
@Setter
@Deprecated(since = "8.0.0", forRemoval = true)
public class WsFederationDelegatedCookieProperties extends PinnableCookieProperties {
    @Serial
    private static final long serialVersionUID = 7392972818105536350L;

    /**
     * Crypto settings that determine how the cookie should be signed and encrypted.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public WsFederationDelegatedCookieProperties() {
        setName("WSFEDDELSESSION");
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
