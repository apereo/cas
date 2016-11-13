package org.apereo.cas.configuration.model.support.jwt;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link JwtTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketRegistryProperties {
    @NestedConfigurationProperty
    private CryptographyProperties crypto = new CryptographyProperties();

    public CryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final CryptographyProperties crypto) {
        this.crypto = crypto;
    }

}
