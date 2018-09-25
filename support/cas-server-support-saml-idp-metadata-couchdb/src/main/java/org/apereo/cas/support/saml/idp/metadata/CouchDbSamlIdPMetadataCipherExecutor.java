package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link CouchDbSamlIdPMetadataCipherExecutor}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {
    public CouchDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    public CouchDbSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }

    @Override
    public String getName() {
        return "CouchDb Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.samlIdp.metadata.couchDb.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.samlIdp.metadata.couchDb.crypto.signing.key";
    }
}
