package org.apereo.cas.authentication.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link CacheCredentialsCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CacheCredentialsCipherExecutor extends BaseStringCipherExecutor {

    /**
     * Instantiates a new cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     * @param alg                 the alg
     */
    public CacheCredentialsCipherExecutor(final String secretKeyEncryption,
                                          final String secretKeySigning,
                                          final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
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
