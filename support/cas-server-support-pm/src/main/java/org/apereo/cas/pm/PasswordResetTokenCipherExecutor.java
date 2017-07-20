package org.apereo.cas.pm;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link PasswordResetTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordResetTokenCipherExecutor extends BaseStringCipherExecutor {
    /**
     * Instantiates a new pm cipher executor.
     *
     * @param secretKeyEncryption                  the secret key encryption
     * @param secretKeySigning                     the secret key signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     */
    public PasswordResetTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                            final String contentEncryptionAlgorithmIdentifier) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier);
    }

    @Override
    public String getName() {
        return "Password Reset Token";
    }
    
    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.pm.reset.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.pm.reset.crypto.signing.key";
    }
}
