package org.apereo.cas.trusted.util.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;

/**
 * {@link CipherExecutor} for a Device Fingerprint cookie.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public class CookieDeviceFingerprintComponentCipherExecutor extends BaseStringCipherExecutor {
    public CookieDeviceFingerprintComponentCipherExecutor(final String secretKeyEncryption,
                                                          final String secretKeySigning,
                                                          final String alg,
                                                          final int signingKeySize,
                                                          final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Cookie MFA Trusted Device Component Cookie";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.signing.key";
    }
}
