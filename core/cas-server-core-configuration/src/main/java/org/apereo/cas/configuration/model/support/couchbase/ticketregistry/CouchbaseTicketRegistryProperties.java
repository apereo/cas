package org.apereo.cas.configuration.model.support.couchbase.ticketregistry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CouchbaseTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */


public class CouchbaseTicketRegistryProperties {

    private boolean queryEnabled = true;

    private String nodeSet = "localhost:8091";

    private String timeout = "PT10S";

    private String password;
    private String bucket = "default";

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

    public boolean isQueryEnabled() {
        return queryEnabled;
    }

    public void setQueryEnabled(final boolean queryEnabled) {
        this.queryEnabled = queryEnabled;
    }

    public String getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(final String nodeSet) {
        this.nodeSet = nodeSet;
    }

    public long getTimeout() {
        return Beans.newDuration(timeout).getSeconds();
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }
}
