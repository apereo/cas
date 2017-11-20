package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Redis.
 *
 * @author serv
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-redis-ticket-registry")
public class RedisTicketRegistryProperties extends BaseRedisProperties {
    private static final long serialVersionUID = -2600996050439638782L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public RedisTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }

}
