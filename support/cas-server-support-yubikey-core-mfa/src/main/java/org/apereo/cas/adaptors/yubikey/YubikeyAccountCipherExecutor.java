package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link YubikeyAccountCipherExecutor}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class YubikeyAccountCipherExecutor extends BaseStringCipherExecutor {

    public YubikeyAccountCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                        final String alg,
                                        final int signingKeySize,
                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    public YubikeyAccountCipherExecutor(final String secretKeyEncryption,
                                        final String secretKeySigning,
                                        final int signingKeySize,
                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "YubiKey Authentication";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.mfa.yubikey.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.mfa.yubikey.crypto.signing.key";
    }
}
