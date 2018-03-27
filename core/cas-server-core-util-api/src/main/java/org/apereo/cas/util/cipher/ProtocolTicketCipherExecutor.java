package org.apereo.cas.util.cipher;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link ProtocolTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class ProtocolTicketCipherExecutor extends BaseStringCipherExecutor {

    /**
     * Instantiates a new Protocol ticket cipher executor.
     *
     * @param secretKeyEncryption                  the secret key encryption
     * @param secretKeySigning                     the secret key signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     */
    public ProtocolTicketCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, 
                                        final String contentEncryptionAlgorithmIdentifier) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier);
    }

    @Override
    public String getName() {
        return "CAS Protocol Tickets";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.ticket.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.ticket.crypto.signing.key";
    }
}
