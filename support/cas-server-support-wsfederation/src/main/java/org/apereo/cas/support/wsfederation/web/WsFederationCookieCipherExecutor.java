package org.apereo.cas.support.wsfederation.web;

import module java.base;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link WsFederationCookieCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
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
    public String getEncryptionKeySetting() {
        return "cas.authn.wsfed[0].cookie.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.wsfed[0].cookie.crypto.signing.key";
    }
}
