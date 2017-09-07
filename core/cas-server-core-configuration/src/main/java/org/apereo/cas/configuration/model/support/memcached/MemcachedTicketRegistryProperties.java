package org.apereo.cas.configuration.model.support.memcached;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link MemcachedTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MemcachedTicketRegistryProperties extends BaseMemcachedProperties {

    private static final long serialVersionUID = 509520518053691786L;
    
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
}



