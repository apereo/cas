package org.apereo.cas.configuration.model.support.couchdb.ticketregistry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CouchDbTicketRegistryProperties}.
 *
 * @author Timur Duehr
 * @see org.apache.http.params.HttpParams
 * @see org.apache.http.params.HttpProtocolParams
 * @see org.apache.http.params.HttpConnectionParams
 * @see org.apache.http.params.BasicHttpParams
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class CouchDbTicketRegistryProperties extends BaseCouchDbProperties {
    private static final long serialVersionUID = 6895485069081125319L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public CouchDbTicketRegistryProperties() {
        this.crypto.setEnabled(false);
        this.setDbName("ticket_registry");
    }
}
