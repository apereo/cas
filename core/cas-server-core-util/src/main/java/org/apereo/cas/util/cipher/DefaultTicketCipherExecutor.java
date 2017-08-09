package org.apereo.cas.util.cipher;

/**
 * This is {@link DefaultTicketCipherExecutor} that handles the encryption
 * and signing of tickets during replication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultTicketCipherExecutor extends BaseBinaryCipherExecutor {
    public DefaultTicketCipherExecutor(final String encryptionSecretKey, final String signingSecretKey,
                                       final String secretKeyAlg, final int signingKeySize,
                                       final int encryptionKeySize, final String cipherName) {
        super(encryptionSecretKey, signingSecretKey, signingKeySize, encryptionKeySize, cipherName);
        setSecretKeyAlgorithm(secretKeyAlg);
    }

    @Override
    public String getName() {
        return "Ticketing";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.ticket.registry." + this.cipherName + ".encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.ticket.registry." + this.cipherName + ".signing.key";
    }
}
