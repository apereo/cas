package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link YubikeyAccountCipherExecutor}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class YubikeyAccountCipherExecutor extends BaseStringCipherExecutor {

    public YubikeyAccountCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
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
