package org.apereo.cas.configuration.model.support.memcached;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link MemcachedTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 7.0.0
 */
@RequiresModule(name = "cas-server-support-memcached-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "7.0.0")
public class MemcachedTicketRegistryProperties extends BaseMemcachedProperties {

    @Serial
    private static final long serialVersionUID = 509520518053691786L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public MemcachedTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
