package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for saml metadata based on JPA.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-jpa")
@Getter
@Setter
@Accessors(chain = true)
public class JpaSamlMetadataProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 352435146313504995L;

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

    public JpaSamlMetadataProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-saml-metadata");
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
