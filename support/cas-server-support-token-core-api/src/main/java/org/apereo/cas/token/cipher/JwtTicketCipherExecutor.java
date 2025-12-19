package org.apereo.cas.token.cipher;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import lombok.val;


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
    public String getEncryptionKeySetting() {
        return "cas.authn.token.crypto.encryption.key";
    }

    @Override
    public String getSigningKeySetting() {
        return "cas.authn.token.crypto.signing.key";
    }

    @Override
    public String getName() {
        return "Token/JWT Tickets";
    }

    /**
     * Gets cipher operations order.
     *
     * @param registeredService the registered service
     * @return the cipher operations order
     */
    protected Optional<CipherOperationsStrategyType> getCipherOperationsStrategyType(final RegisteredService registeredService) {
        val property = getCipherStrategyTypeRegisteredServiceProperty(registeredService);
        if (property.isAssignedTo(registeredService)) {
            val order = property.getPropertyValue(registeredService).value();
            return Optional.of(CipherOperationsStrategyType.valueOf(order));
        }
        return Optional.empty();
    }

    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherStrategyTypeRegisteredServiceProperty(final RegisteredService registeredService) {
        return RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_CIPHER_STRATEGY_TYPE;
    }
    
}
