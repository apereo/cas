package org.apereo.cas.configuration.model.support.mongo.ticketregistry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link MongoTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-mongo-ticket-registry")
@Getter
@Setter
public class MongoTicketRegistryProperties extends BaseMongoDbProperties {

    private static final long serialVersionUID = 8243690796900311918L;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public MongoTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
