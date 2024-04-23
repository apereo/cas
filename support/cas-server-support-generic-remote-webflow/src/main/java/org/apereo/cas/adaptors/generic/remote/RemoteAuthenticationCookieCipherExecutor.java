package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link RemoteAuthenticationCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RemoteAuthenticationCookieCipherExecutor extends BaseStringCipherExecutor {

    public RemoteAuthenticationCookieCipherExecutor(final String secretKeyEncryption,
                                                    final String secretKeySigning,
                                                    final String alg,
                                                    final int signingKeySize,
                                                    final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Remote Cookie Authentication";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.remote.cookie.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.remote.cookie.crypto.signing.key";
    }
}
