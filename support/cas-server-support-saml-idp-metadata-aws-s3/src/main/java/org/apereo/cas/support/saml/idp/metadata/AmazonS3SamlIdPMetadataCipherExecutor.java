package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link AmazonS3SamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AmazonS3SamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {
    public AmazonS3SamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                 final String alg,
                                                 final int signingKeySize,
                                                 final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Amazon S3 Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.amazon-s3.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.amazon-s3.crypto.signing.key";
    }
}

