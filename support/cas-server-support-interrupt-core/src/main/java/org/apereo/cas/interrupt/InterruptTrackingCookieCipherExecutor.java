package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link InterruptTrackingCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class InterruptTrackingCookieCipherExecutor extends BaseStringCipherExecutor {
    public InterruptTrackingCookieCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                 final String alg, final int signingKeySize,
                                                 final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Interrupt Webflow Tracking Cookie";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.interrupt.cookie.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.interrupt.cookie.crypto.signing.key";
    }
}


