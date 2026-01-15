package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link DuoSecurityAuthenticationRegistrationCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DuoSecurityAuthenticationRegistrationCipherExecutor extends BaseStringCipherExecutor {

    public DuoSecurityAuthenticationRegistrationCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                                               final String alg, final boolean encryptionEnabled,
                                                               final boolean signingEnabled, final int signingKeySize,
                                                               final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled,
            signingEnabled, signingKeySize, encryptionKeySize);
    }
    
    @Override
    public String getName() {
        return "Duo Security Registration";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.authn.mfa.duo[0].registration.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.mfa.duo[0].registration.crypto.signing.key";
    }
}
