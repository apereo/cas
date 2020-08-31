package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link GitSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class GitSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public GitSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                            final String secretKeySigning,
                                            final String alg,
                                            final int signingKeySize,
                                            final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Git Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.git.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.git.crypto.signing.key";
    }
}
