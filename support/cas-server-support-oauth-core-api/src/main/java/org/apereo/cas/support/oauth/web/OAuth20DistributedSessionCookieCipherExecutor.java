package org.apereo.cas.support.oauth.web;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link OAuth20DistributedSessionCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 * @deprecated Since 7.3.0.
 */
@Deprecated(since = "7.3.0", forRemoval = true)
public class OAuth20DistributedSessionCookieCipherExecutor extends BaseStringCipherExecutor {
    public OAuth20DistributedSessionCookieCipherExecutor(final String secretKeyEncryption,
                                                         final String secretKeySigning,
                                                         final String alg,
                                                         final int signingKeySize,
                                                         final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "OAuth/OIDC Distributed Session Management Cookie";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.oauth.session-replication.cookie.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.oauth.session-replication.cookie.crypto.signing.key";
    }
}


