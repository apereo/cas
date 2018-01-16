package org.apereo.cas.trusted.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link MultifactorAuthenticationTrustCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class MultifactorAuthenticationTrustCipherExecutor extends BaseStringCipherExecutor {
    public MultifactorAuthenticationTrustCipherExecutor(final String secretKeyEncryption,
                                                        final String secretKeySigning,
                                                        final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    @Override
    public String getName() {
        return "Multifactor Authentication & Trusted Devices";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.mfa.trusted.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.mfa.trusted.crypto.signing.key";
    }
}
