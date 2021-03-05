package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link JpaSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class JpaSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public JpaSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                            final String alg,
                                            final int signingKeySize,
                                            final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    public JpaSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                            final int signingKeySize,
                                            final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "JPA Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.jpa.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.jpa.crypto.signing.key";
    }
}
