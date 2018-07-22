package org.apereo.cas.token.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link TokenTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TokenTicketCipherExecutor extends BaseStringCipherExecutor {
    public TokenTicketCipherExecutor() {
        this(null, null, null, false, false);
    }

    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning,
                                     final String alg,
                                     final boolean encryptionEnabled,
                                     final boolean signingEnabled) {
        super(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled, signingEnabled);
    }


    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning,
                                     final String alg,
                                     final boolean encryptionEnabled) {
        this(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled, true);
    }

    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning,
                                     final boolean encryptionEnabled) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled);
    }

    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning,
                                     final boolean encryptionEnabled,
                                     final boolean signingEnabled) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled, signingEnabled);
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
