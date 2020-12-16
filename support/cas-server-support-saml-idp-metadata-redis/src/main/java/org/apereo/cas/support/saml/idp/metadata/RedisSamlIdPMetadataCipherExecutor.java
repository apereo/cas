package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link RedisSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class RedisSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public RedisSamlIdPMetadataCipherExecutor(final String secretKeyEncryption,
                                                final String secretKeySigning,
                                                final String alg,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Redis Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.metadata.redis.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.saml-idp.metadata.redis.crypto.signing.key";
    }
}
