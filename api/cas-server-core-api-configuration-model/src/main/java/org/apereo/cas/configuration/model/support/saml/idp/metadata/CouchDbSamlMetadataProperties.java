package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for saml metadata based on CouchDB.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-couchdb")
@Getter
@Setter
public class CouchDbSamlMetadataProperties extends BaseCouchDbProperties {

    private static final long serialVersionUID = 1673956475847790139L;

    /**
     * Whether identity provider metadata artifacts
     * are expected to be found in the database.
     */
    private boolean idpMetadataEnabled;

    /**
     * Crypto settings that sign/encrypt the metadata records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public CouchDbSamlMetadataProperties() {
        setDbName("saml_metadata");
    }
}
