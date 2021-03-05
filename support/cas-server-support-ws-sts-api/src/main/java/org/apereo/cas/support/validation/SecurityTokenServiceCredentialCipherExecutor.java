package org.apereo.cas.support.validation;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link SecurityTokenServiceCredentialCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SecurityTokenServiceCredentialCipherExecutor extends BaseStringCipherExecutor {
    public SecurityTokenServiceCredentialCipherExecutor(final String secretKeyEncryption,
                                                        final String secretKeySigning,
                                                        final String alg,
                                                        final int signingKeySize,
                                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "WSFederation Security Token Service";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.wsfed-idp.sts.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.wsfed-idp.sts.crypto.signing.key";
    }
}
