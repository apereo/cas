package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
@JsonFilter("CouchDbSamlMetadataProperties")
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
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
