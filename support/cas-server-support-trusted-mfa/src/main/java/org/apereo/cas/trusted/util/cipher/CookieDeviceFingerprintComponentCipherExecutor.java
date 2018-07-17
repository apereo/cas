package org.apereo.cas.trusted.util.cipher;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * {@link CipherExecutor} for a Device Fingerprint cookie.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public class CookieDeviceFingerprintComponentCipherExecutor extends BaseStringCipherExecutor {
    public CookieDeviceFingerprintComponentCipherExecutor(final String secretKeyEncryption,
                                                          final String secretKeySigning,
                                                          final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    @Override
    public String getName() {
        return "Cookie MFA Trusted Device Component Cookie";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.mfa.trusted.deviceFingerprint.cookie.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.mfa.trusted.deviceFingerprint.cookie.crypto.signing.key";
    }
}
