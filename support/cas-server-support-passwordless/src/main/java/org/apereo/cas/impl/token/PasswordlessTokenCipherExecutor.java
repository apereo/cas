package org.apereo.cas.impl.token;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link PasswordlessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PasswordlessTokenCipherExecutor extends BaseStringCipherExecutor {
    public PasswordlessTokenCipherExecutor(final String secretKeyEncryption,
                                           final String secretKeySigning,
                                           final String alg,
                                           final int signingKeySize,
                                           final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.passwordless.tokens.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.passwordless.tokens.crypto.signing.key";
    }

    @Override
    public String getName() {
        return "Passwordless Authentication";
    }
}
