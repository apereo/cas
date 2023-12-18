package org.apereo.cas.util.cipher;


import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;

/**
 * This is {@link ProtocolTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ProtocolTicketCipherExecutor extends BaseStringCipherExecutor {

    public ProtocolTicketCipherExecutor() {
        super(null, null, EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, 0, 0);
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
        super(secretKeyEncryption, secretKeySigning, EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, signingKeySize, encryptionKeySize);
    }

    @Override
    public String getName() {
        return "CAS Protocol Tickets";
    }

    @Override
    public String getEncryptionKeySetting() {
        return "cas.ticket.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.ticket.crypto.signing.key";
    }
}
