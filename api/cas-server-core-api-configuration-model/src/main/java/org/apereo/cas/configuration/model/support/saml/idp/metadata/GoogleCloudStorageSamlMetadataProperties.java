package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link GoogleCloudStorageSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-gcp-storage")
@Getter
@Setter
@Accessors(chain = true)
public class GoogleCloudStorageSamlMetadataProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -7226473583467202828L;

    /**
     * Crypto settings that sign/encrypt the metadata records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();
}
