package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link MongoDbSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public MongoDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                final String secretKeySigning,
                                                final String alg,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    public MongoDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                final String secretKeySigning,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "MongoDb Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.mongo.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.mongo.crypto.signing.key";
    }
}
