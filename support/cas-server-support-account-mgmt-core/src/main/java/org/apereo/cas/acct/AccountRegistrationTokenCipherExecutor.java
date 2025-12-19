package org.apereo.cas.acct;

import module java.base;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link AccountRegistrationTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class AccountRegistrationTokenCipherExecutor extends BaseStringCipherExecutor {

    public AccountRegistrationTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                  final String contentEncryptionAlgorithmIdentifier,
                                                  final int signingKeySize,
                                                  final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier,
            signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "Account Registration Token";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.account-registration.core.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.account-registration.core.crypto.signing.key";
    }
}
