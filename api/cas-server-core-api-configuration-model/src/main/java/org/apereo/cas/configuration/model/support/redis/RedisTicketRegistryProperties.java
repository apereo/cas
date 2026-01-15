package org.apereo.cas.configuration.model.support.redis;

import module java.base;
import org.apereo.cas.configuration.model.core.cache.SimpleCacheProperties;
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

    @Serial
    private static final long serialVersionUID = -2600996050439638782L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    /**
     * Control second-level cache settings
     * that keeps ticket in memory.
     */
    @NestedConfigurationProperty
    private SimpleCacheProperties cache = new SimpleCacheProperties();

    /**
     * Identifier for this CAS server node
     * that tags the sender/receiver in the queue
     * and avoid processing of inbound calls.
     * If left blank, an identifier is generated automatically
     * and kept in memory.
     */
    private String queueIdentifier;

    /**
     * Allows the register to detect the presence of Redis modules,
     * in particular RediSearch, which allows the registry to create specific
     * indexes and search Redis documents to look up tickets. Enabling indexing
     * and searching capabilities may lead to significant performance improvements.
     */
    private boolean enableRedisSearch = true;

    public RedisTicketRegistryProperties() {
        crypto.setEnabled(false);
    }
}
