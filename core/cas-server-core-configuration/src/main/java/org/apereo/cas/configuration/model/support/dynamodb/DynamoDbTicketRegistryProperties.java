package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link DynamoDbTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbTicketRegistryProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = 699497009058965681L;
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private CryptographyProperties crypto = new CryptographyProperties();

    public CryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final CryptographyProperties crypto) {
        this.crypto = crypto;
    }
}
