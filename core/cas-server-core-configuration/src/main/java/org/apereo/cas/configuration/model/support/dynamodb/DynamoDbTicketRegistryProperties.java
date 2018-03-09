package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link DynamoDbTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-ticket-registry")
public class DynamoDbTicketRegistryProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = 699497009058965681L;

    /**
     * The table name used and created by CAS to hold service tickets in DynamoDb.
     */
    private String serviceTicketsTableName = "serviceTicketsTable";

    public String getServiceTicketsTableName() {
        return serviceTicketsTableName;
    }

    public void setServiceTicketsTableName(final String serviceTicketsTableName) {
        this.serviceTicketsTableName = serviceTicketsTableName;
    }

    /**
     * The table name used and created by CAS to hold proxy tickets in DynamoDb.
     */
    private String proxyTicketsTableName = "proxyTicketsTable";

    public String getProxyTicketsTableName() {
        return proxyTicketsTableName;
    }

    public void setProxyTicketsTableName(final String proxyTicketsTableName) {
        this.proxyTicketsTableName = proxyTicketsTableName;
    }

    /**
     * The table name used and created by CAS to hold ticket granting tickets in DynamoDb.
     */
    private String ticketGrantingTicketsTableName = "ticketGrantingTicketsTable";

    public String getTicketGrantingTicketsTableName() {
        return ticketGrantingTicketsTableName;
    }

    public void setTicketGrantingTicketsTableName(final String ticketGrantingTicketsTableName) {
        this.ticketGrantingTicketsTableName = ticketGrantingTicketsTableName;
    }

    /**
     * The table name used and created by CAS to hold proxy ticket granting tickets in DynamoDb.
     */
    private String proxyGrantingTicketsTableName = "proxyGrantingTicketsTable";

    public String getProxyGrantingTicketsTableName() {
        return proxyGrantingTicketsTableName;
    }

    public void setProxyGrantingTicketsTableName(final String proxyGrantingTicketsTableName) {
        this.proxyGrantingTicketsTableName = proxyGrantingTicketsTableName;
    }

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public DynamoDbTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
    
    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
}
