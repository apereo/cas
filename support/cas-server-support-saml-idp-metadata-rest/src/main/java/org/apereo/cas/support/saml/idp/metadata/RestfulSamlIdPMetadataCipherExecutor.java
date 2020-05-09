package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link RestfulSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class RestfulSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public RestfulSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                final String secretKeySigning,
                                                final String alg,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    public RestfulSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                final String secretKeySigning,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Restful Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.rest.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.rest.crypto.signing.key";
    }
}
