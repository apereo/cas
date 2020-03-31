package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Redis.
 *
 * @author serv
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-redis-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
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
}
