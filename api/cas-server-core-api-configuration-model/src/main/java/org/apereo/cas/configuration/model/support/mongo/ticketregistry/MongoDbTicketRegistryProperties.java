package org.apereo.cas.configuration.model.support.mongo.ticketregistry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link MongoDbTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-mongo-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbTicketRegistryProperties extends BaseMongoDbProperties {

    @Serial
    private static final long serialVersionUID = 8243690796900311918L;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    /**
     * Whether CAS should attempt to create/update indexes automatically
     * and figure out the differences between existing keys and new keys.
     */
    private boolean updateIndexes = true;

    /**
     * When updating/creating indexes, decide if existing indexes
     * should all be dropped once prior to creating/updating indexes.
     * This may be useful to avoid conflicts between old and new indexes,
     * in scenarios where CAS may be unable to locate the proper difference
     * in index options or names during upgrades..
     */
    private boolean dropIndexes;

    /**
     * Index names to create. By default, all indexes are created.
     * Supported indexes are:
     * <ul>
     *     <li>{@code IDX_ID}: index created for ticket identifiers.</li>
     *     <li>{@code IDX_PRINCIPAL}: index created for principal attached to the ticket.</li>
     *     <li>{@code IDX_EXPIRATION}: index created for ticket expiration date.</li>
     *</ul>
     */
    private List<String> indexes = new ArrayList<>();
    
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public MongoDbTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
