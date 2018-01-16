package org.apereo.cas.util.cipher;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link DefaultTicketCipherExecutor} that handles the encryption
 * and signing of tickets during replication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
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
