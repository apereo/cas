package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;

/**
 * This is {@link DynamoDbSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-dynamodb")
@Getter
@Setter
@Accessors(chain = true)
public class DynamoDbSamlMetadataProperties extends AbstractDynamoDbProperties {

    @Serial
    private static final long serialVersionUID = -127192724742371662L;

    /**
     * Crypto settings that sign/encrypt the metadata records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * The table name used and created by CAS to hold SAML service provider metadata in DynamoDb.
     */
    private String tableName = "DynamoDbCasSamlMetadata";

    /**
     * The table name used and created by CAS to hold saml idp metadata in DynamoDb.
     * By default, leaving this setting blank will prevent CAS from storing
     * or loading SAML idp metadata from DynamoDb.
     * Example: {@code DynamoDbCasSamlIdPMetadata}
     */
    private String idpMetadataTableName;

    public DynamoDbSamlMetadataProperties() {
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
