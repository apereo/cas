package org.apereo.cas.support.validation;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link SecurityTokenServiceCredentialCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SecurityTokenServiceCredentialCipherExecutor extends BaseStringCipherExecutor {
    public SecurityTokenServiceCredentialCipherExecutor(final String secretKeyEncryption,
                                                        final String secretKeySigning,
                                                        final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    @Override
    public String getName() {
        return "WSFederation Security Token Service";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.wsfedIdP.sts.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.wsfedIdP.sts.crypto.signing.key";
    }
}
