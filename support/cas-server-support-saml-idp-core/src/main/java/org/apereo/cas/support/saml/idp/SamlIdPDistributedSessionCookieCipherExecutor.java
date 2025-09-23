package org.apereo.cas.support.saml.idp;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link SamlIdPDistributedSessionCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 * @deprecated Since 7.3.0.
 */
@Deprecated(since = "7.3.0", forRemoval = true)
public class SamlIdPDistributedSessionCookieCipherExecutor extends BaseStringCipherExecutor {
    public SamlIdPDistributedSessionCookieCipherExecutor(final String secretKeyEncryption,
                                                         final String secretKeySigning,
                                                         final String alg,
                                                         final int signingKeySize,
                                                         final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "SAML2 Identity Provider Distributed Session Management Cookie";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.saml-idp.core.session-replication.cookie.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.saml-idp.core.session-replication.cookie.crypto.signing.key";
    }
}

