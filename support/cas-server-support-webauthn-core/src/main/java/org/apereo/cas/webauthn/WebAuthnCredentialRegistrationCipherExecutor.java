package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link WebAuthnCredentialRegistrationCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class WebAuthnCredentialRegistrationCipherExecutor extends BaseStringCipherExecutor {
    public WebAuthnCredentialRegistrationCipherExecutor(final String secretKeyEncryption,
                                                        final String secretKeySigning,
                                                        final String alg,
                                                        final int signingKeySize,
                                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "WebAuthN Authentication";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.mfa.web-authn.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.mfa.web-authn.crypto.signing.key";
    }
}
