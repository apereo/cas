package org.apereo.cas.consent;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link AttributeReleaseConsentCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class AttributeReleaseConsentCipherExecutor extends BaseStringCipherExecutor {

    public AttributeReleaseConsentCipherExecutor(final String secretKeyEncryption,
                                                 final String secretKeySigning,
                                                 final String alg,
                                                 final int signingKeySize,
                                                 final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.consent.core.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.consent.core.crypto.signing.key";
    }

    @Override
    public String getName() {
        return "Attribute Consent";
    }
}
