package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link GoogleCloudStorageSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class GoogleCloudStorageSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public GoogleCloudStorageSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                           final String secretKeySigning,
                                                           final String alg,
                                                           final int signingKeySize,
                                                           final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Google Cloud Storage Saml IdP Metadata";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.gcp.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.gcp.crypto.signing.key";
    }
}
