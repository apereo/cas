package org.apereo.cas.util.cipher;

/**
 * This is {@link TicketGrantingCookieCipherExecutor} that reads TGC keys from the CAS config
 * and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class TicketGrantingCookieCipherExecutor extends BaseStringCipherExecutor {
    
    public TicketGrantingCookieCipherExecutor(final String secretKeyEncryption,
                                              final String secretKeySigning,
                                              final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    public TicketGrantingCookieCipherExecutor(final String secretKeyEncryption,
                                              final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }

    @Override
    public String getName() {
        return "Ticket-granting Cookie";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.tgc.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.tgc.crypto.signing.key";
    }
}
