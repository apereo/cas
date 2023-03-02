package org.apereo.cas.configuration.model.support.cosmosdb;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;

/**
 * This is {@link CosmosDbTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class CosmosDbTicketRegistryProperties extends BaseCosmosDbProperties {
    @Serial
    private static final long serialVersionUID = 3528153816791719898L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public CosmosDbTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
