package org.apereo.cas.configuration.model.support.couchdb;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link CouchDbTicketRegistryProperties}.
 * @see org.apache.http.params.HttpParams
 * @see org.apache.http.params.HttpProtocolParams
 * @see org.apache.http.params.HttpConnectionParams
 * @see org.apache.http.params.BasicHttpParams
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-ticket-registry")
@Slf4j
@Getter
@Setter
public class CouchDbTicketRegistryProperties extends AbstractCouchDbProperties {
    private static final long serialVersionUID = 6895485069081125319L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public CouchDbTicketRegistryProperties() {
        this.crypto.setEnabled(false);
        this.setDbName("ticketRegistry");
    }
}
