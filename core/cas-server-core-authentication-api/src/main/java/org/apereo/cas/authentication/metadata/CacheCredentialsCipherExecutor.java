package org.apereo.cas.authentication.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link CacheCredentialsCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CacheCredentialsCipherExecutor extends BaseStringCipherExecutor {

    public CacheCredentialsCipherExecutor(final String secretKeyEncryption,
                                          final String secretKeySigning,
                                          final String alg,
                                          final int signingKeySize,
                                          final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }


    @Override
    public String getName() {
        return "Credential Caching & Clearpass";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.clearpass.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.clearpass.crypto.signing.key";
    }
}
