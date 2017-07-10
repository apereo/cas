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
                                 final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.consent.encryptionKey";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.consent.signingKey";
    }

    @Override
    public String getName() {
        return "Attribute Consent";
    }
}
