package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link DynamoDbSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class DynamoDbSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public DynamoDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                 final String secretKeySigning,
                                                 final String alg,
                                                 final int signingKeySize,
                                                 final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "DynamoDb Saml IdP Metadata";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.dynamo-db.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.dynamo-db.crypto.signing.key";
    }
}
