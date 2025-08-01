package org.apereo.cas.web;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 * @deprecated Since 7.3.0.
 */
@Deprecated(since = "7.3.0", forRemoval = true)
public class DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor extends BaseStringCipherExecutor {
    public DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor(final String secretKeyEncryption,
                                                                               final String secretKeySigning,
                                                                               final String alg,
                                                                               final int signingKeySize,
                                                                               final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Delegated Authentication Distributed Session Management Cookie";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.pac4j.core.session-replication.cookie.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.pac4j.core.session-replication.cookie.crypto.signing.key";
    }
}



