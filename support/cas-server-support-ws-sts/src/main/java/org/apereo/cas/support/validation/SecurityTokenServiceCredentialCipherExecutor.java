package org.apereo.cas.support.validation;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link SecurityTokenServiceCredentialCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SecurityTokenServiceCredentialCipherExecutor extends BaseStringCipherExecutor {
    public SecurityTokenServiceCredentialCipherExecutor(final String secretKeyEncryption, final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }

    @Override
    public String getName() {
        return "WSFederation Security Token Service";
    }
}
