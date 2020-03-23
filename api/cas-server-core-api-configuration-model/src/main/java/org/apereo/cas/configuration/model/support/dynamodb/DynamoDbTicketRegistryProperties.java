package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link DynamoDbTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class DynamoDbTicketRegistryProperties extends AbstractDynamoDbProperties {

    private static final long serialVersionUID = 699497009058965681L;

    /**
     * The table name used and created by CAS to hold service tickets in DynamoDb.
     */
    private String serviceTicketsTableName = "serviceTicketsTable";

    /**
     * The table name used and created by CAS to hold proxy tickets in DynamoDb.
     */
    private String proxyTicketsTableName = "proxyTicketsTable";

    /**
     * The table name used and created by CAS to hold ticket granting tickets in DynamoDb.
     */
    private String ticketGrantingTicketsTableName = "ticketGrantingTicketsTable";

    /**
     * The table name used and created by CAS to hold proxy ticket granting tickets in DynamoDb.
     */
    private String proxyGrantingTicketsTableName = "proxyGrantingTicketsTable";

    /**
     * The table name used and created by CAS to hold transient session ticket tickets in DynamoDb.
     */
    private String transientSessionTicketsTableName = "transientSessionTicketsTable";

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public DynamoDbTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
