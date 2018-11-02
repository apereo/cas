package org.apereo.cas.util.cipher;


/**
 * This is {@link ProtocolTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ProtocolTicketCipherExecutor extends BaseStringCipherExecutor {

    public ProtocolTicketCipherExecutor() {
        super(null, null, DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, 0, 0);
    }

    public ProtocolTicketCipherExecutor(final String secretKeyEncryption,
                                        final String secretKeySigning,
                                        final String contentEncryptionAlgorithmIdentifier,
                                        final int signingKeySize,
                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier,
            signingKeySize, encryptionKeySize);
    }

    public ProtocolTicketCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                        final int signingKeySize,
                                        final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, signingKeySize, encryptionKeySize);
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
