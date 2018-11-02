package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link WsFederationCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WsFederationCookieCipherExecutor extends BaseStringCipherExecutor {
    public WsFederationCookieCipherExecutor(final String secretKeyEncryption,
                                            final String secretKeySigning,
                                            final String alg,
                                            final int signingKeySize,
                                            final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "WsFederation Delegated Authentication";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.wsfed[].cookie.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.wsfed[].cookie.crypto.signing.key";
    }
}
