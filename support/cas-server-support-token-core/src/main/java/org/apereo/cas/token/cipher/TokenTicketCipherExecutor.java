package org.apereo.cas.token.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link TokenTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TokenTicketCipherExecutor extends BaseStringCipherExecutor {
    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning,
                                     final String alg, 
                                     final boolean encryptionEnabled) {
        super(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled);
    }

    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning,
                                     final boolean encryptionEnabled) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled);
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.token.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.token.crypto.signing.key";
    }

    @Override
    public String getName() {
        return "Token/JWT Tickets";
    }
}
