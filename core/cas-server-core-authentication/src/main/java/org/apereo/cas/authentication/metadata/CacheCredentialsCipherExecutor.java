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
     */
    public CacheCredentialsCipherExecutor(final String secretKeyEncryption,
                                          final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }


    @Override
    public String getName() {
        return "Credential Caching & Clearpass";
    }
}
