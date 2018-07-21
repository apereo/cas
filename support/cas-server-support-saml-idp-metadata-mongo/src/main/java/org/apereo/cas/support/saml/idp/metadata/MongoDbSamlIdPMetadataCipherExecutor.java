package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link MongoDbSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public MongoDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    public MongoDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }

    @Override
    public String getName() {
        return "MongoDb Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.samlIdp.metadata.mongo.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.samlIdp.metadata.mongo.crypto.signing.key";
    }
}
