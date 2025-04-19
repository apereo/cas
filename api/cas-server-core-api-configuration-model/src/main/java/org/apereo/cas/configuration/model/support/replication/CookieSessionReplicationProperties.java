package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;

/**
 * This is {@link CookieSessionReplicationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-pac4j-api")
@AllArgsConstructor
@With
public class CookieSessionReplicationProperties extends PinnableCookieProperties {
    /**
     * Default cookie name.
     */
    public static final String DEFAULT_COOKIE_NAME = "DISSESSION";

    @Serial
    private static final long serialVersionUID = 6165162204295764362L;

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

    public CookieSessionReplicationProperties() {
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
