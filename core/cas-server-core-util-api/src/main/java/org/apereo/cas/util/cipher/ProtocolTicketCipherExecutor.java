package org.apereo.cas.util.cipher;


/**
 * This is {@link ProtocolTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ProtocolTicketCipherExecutor extends BaseStringCipherExecutor {

    public ProtocolTicketCipherExecutor() {
        super(null, null, DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
    }

    public ProtocolTicketCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                        final String contentEncryptionAlgorithmIdentifier) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier);
    }

    public ProtocolTicketCipherExecutor(final String secretKeyEncryption, final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning, DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
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
