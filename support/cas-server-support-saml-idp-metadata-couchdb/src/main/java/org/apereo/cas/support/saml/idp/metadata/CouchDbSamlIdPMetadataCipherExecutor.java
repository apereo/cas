package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link CouchDbSamlIdPMetadataCipherExecutor}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {
    public CouchDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                final String alg,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    public CouchDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "CouchDb Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.couch-db.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.couch-db.crypto.signing.key";
    }
}
