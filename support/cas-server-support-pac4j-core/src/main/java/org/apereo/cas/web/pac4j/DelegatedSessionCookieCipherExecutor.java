package org.apereo.cas.web.pac4j;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link DelegatedSessionCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DelegatedSessionCookieCipherExecutor extends BaseStringCipherExecutor {
    public DelegatedSessionCookieCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    public DelegatedSessionCookieCipherExecutor(final String secretKeyEncryption,
                                                final String secretKeySigning,
                                                final String alg,
                                                final int signingKeySize,
                                                final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Delegated Authentication";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.pac4j.cookie.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.pac4j.cookie.crypto.signing.key";
    }
}
