package org.apereo.cas.token.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;


/**
 * This is {@link JwtTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketCipherExecutor extends BaseStringCipherExecutor {
    public JwtTicketCipherExecutor() {
        this(null, null, null, false, false, 0, 0);
    }

    public JwtTicketCipherExecutor(final String secretKeyEncryption,
                                   final String secretKeySigning,
                                   final String contentEncryptionAlgorithmIdentifier,
                                   final boolean encryptionEnabled,
                                   final boolean signingEnabled,
                                   final int signingKeySize,
                                   final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier, encryptionEnabled,
            signingEnabled, signingKeySize, encryptionKeySize);
    }


    public JwtTicketCipherExecutor(final String secretKeyEncryption,
                                   final String secretKeySigning,
                                   final String contentEncryptionAlgorithmIdentifier,
                                   final boolean encryptionEnabled,
                                   final int signingKeySize,
                                   final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier, encryptionEnabled,
            true, signingKeySize, encryptionKeySize);
    }

    public JwtTicketCipherExecutor(final String secretKeyEncryption,
                                   final String secretKeySigning,
                                   final boolean encryptionEnabled,
                                   final int signingKeySize,
                                   final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled,
            signingKeySize, encryptionKeySize);
    }

    public JwtTicketCipherExecutor(final String secretKeyEncryption,
                                   final String secretKeySigning,
                                   final boolean encryptionEnabled,
                                   final boolean signingEnabled,
                                   final int signingKeySize,
                                   final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled,
            signingEnabled, signingKeySize, encryptionKeySize);
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
