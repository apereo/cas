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
                                              final String alg,
                                              final int signingKeySize,
                                              final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, signingKeySize, encryptionKeySize);
    }

    public TicketGrantingCookieCipherExecutor(final String secretKeyEncryption,
                                              final String secretKeySigning,
                                              final int signingKeySize,
                                              final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
    }

    public TicketGrantingCookieCipherExecutor() {
        super(null, null, 0, 0);
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
