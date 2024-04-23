package org.apereo.cas.trusted.authentication;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link MultifactorAuthenticationTrustCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MultifactorAuthenticationTrustCipherExecutor extends BaseStringCipherExecutor {
    public MultifactorAuthenticationTrustCipherExecutor(final String secretKeyEncryption,
                                                        final String secretKeySigning,
                                                        final String alg,
                                                        final int signingKeySize,
                                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Multifactor Authentication & Trusted Devices";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.mfa.trusted.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.mfa.trusted.crypto.signing.key";
    }
}
