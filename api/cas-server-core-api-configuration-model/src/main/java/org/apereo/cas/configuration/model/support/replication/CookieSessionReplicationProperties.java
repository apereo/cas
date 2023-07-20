package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
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
@JsonFilter("CookieSessionReplicationProperties")
public class CookieSessionReplicationProperties extends PinnableCookieProperties {
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
        setName("DISSESSION" + RandomUtils.randomNumeric(4));
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
