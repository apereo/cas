package org.apereo.cas.consent;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link ConsentCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConsentCipherExecutor extends BaseStringCipherExecutor {

    public ConsentCipherExecutor(final String secretKeyEncryption,
                                 final String secretKeySigning,
                                 final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.consent.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.consent.crypto.signing.key";
    }

    @Override
    public String getName() {
        return "Attribute Consent";
    }
}
