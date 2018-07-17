package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import lombok.Getter;

/**
 * This is {@link OneTimeTokenAccountCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class OneTimeTokenAccountCipherExecutor extends BaseStringCipherExecutor {
    private final String name;

    public OneTimeTokenAccountCipherExecutor(final String secretKeyEncryption,
                                             final String secretKeySigning, final String alg, final String name) {
        super(secretKeyEncryption, secretKeySigning, alg);
        this.name = name;
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.mfa.gauth.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.mfa.gauth.crypto.signing.key";
    }
}
