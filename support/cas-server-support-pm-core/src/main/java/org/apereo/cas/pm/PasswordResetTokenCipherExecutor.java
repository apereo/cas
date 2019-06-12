package org.apereo.cas.pm;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link PasswordResetTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordResetTokenCipherExecutor extends BaseStringCipherExecutor {

    public PasswordResetTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                            final String contentEncryptionAlgorithmIdentifier,
                                            final int signingKeySize,
                                            final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier, signingKeySize, encryptionKeySize);
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
