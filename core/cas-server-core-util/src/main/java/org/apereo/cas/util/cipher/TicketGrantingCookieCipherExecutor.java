package org.apereo.cas.util.cipher;

/**
 * This is {@link TicketGrantingCookieCipherExecutor} that reads TGC keys from the CAS config
 * and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class TicketGrantingCookieCipherExecutor extends BaseStringCipherExecutor {

    /**
     * Instantiates a new Tgc cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     */
    public TicketGrantingCookieCipherExecutor(final String secretKeyEncryption,
                                              final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }


    @Override
    public String getName() {
        return "Ticket-granting Cookie";
    }
}
