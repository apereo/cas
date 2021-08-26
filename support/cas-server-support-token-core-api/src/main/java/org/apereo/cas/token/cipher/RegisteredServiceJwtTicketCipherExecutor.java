package org.apereo.cas.token.cipher;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * This is {@link RegisteredServiceJwtTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@NoArgsConstructor
public class RegisteredServiceJwtTicketCipherExecutor extends JwtTicketCipherExecutor implements RegisteredServiceCipherExecutor {

    @Override
    public String decode(final String data, final Optional<RegisteredService> service) {
        if (service.isPresent()) {
            val registeredService = service.get();
            if (supports(registeredService)) {
                LOGGER.debug("Found signing and/or encryption keys for [{}] in service registry to decode", registeredService.getServiceId());
                val cipher = getTokenTicketCipherExecutorForService(registeredService);
                if (cipher.isEnabled()) {
                    return cipher.decode(data, new Object[]{registeredService});
                }
            }
        }
        return decode(data, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    @Override
    public String encode(final String data, final Optional<RegisteredService> service) {
        if (service.isPresent()) {
            val registeredService = service.get();
            if (supports(registeredService)) {
                LOGGER.debug("Found signing and/or encryption keys for [{}] in service registry to encode", registeredService.getServiceId());
                val cipher = getTokenTicketCipherExecutorForService(registeredService);
                if (cipher.isEnabled()) {
                    return cipher.encode(data);
                }
            }
        }
        return encode(data, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    @Override
    public boolean supports(final RegisteredService registeredService) {
        return getSigningKey(registeredService).isPresent();
    }

    /**
     * Gets token ticket cipher executor for service.
     *
     * @param registeredService the registered service
     * @return the token ticket cipher executor for service
     */
    public JwtTicketCipherExecutor getTokenTicketCipherExecutorForService(final RegisteredService registeredService) {
        val encryptionKey = getEncryptionKey(registeredService).orElse(StringUtils.EMPTY);
        val signingKey = getSigningKey(registeredService).orElse(StringUtils.EMPTY);
        val order = getCipherOperationsStrategyType(registeredService).orElse(CipherOperationsStrategyType.ENCRYPT_AND_SIGN);
        return createCipherExecutorInstance(encryptionKey, signingKey, registeredService, order);
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
            val order = property.getPropertyValue(registeredService).getValue();
            return Optional.of(CipherOperationsStrategyType.valueOf(order));
        }
        return Optional.empty();
    }
    
    /**
     * Create cipher executor instance.
     *
     * @param encryptionKey     the encryption key
     * @param signingKey        the signing key
     * @param registeredService the registered service
     * @param order             the order
     * @return the jwt ticket cipher executor
     */
    protected JwtTicketCipherExecutor createCipherExecutorInstance(final String encryptionKey, final String signingKey,
                                                                   final RegisteredService registeredService,
                                                                   final CipherOperationsStrategyType order) {
        val cipher = new JwtTicketCipherExecutor(encryptionKey, signingKey,
            StringUtils.isNotBlank(encryptionKey), StringUtils.isNotBlank(signingKey), 0, 0);
        cipher.setCustomHeaders(CollectionUtils.wrap(CUSTOM_HEADER_REGISTERED_SERVICE_ID, registeredService.getId()));
        cipher.setStrategyType(order);
        return cipher;
    }

    /**
     * Gets signing key.
     *
     * @param registeredService the registered service
     * @return the signing key
     */
    public Optional<String> getSigningKey(final RegisteredService registeredService) {
        val property = getSigningKeyRegisteredServiceProperty();
        if (property.isAssignedTo(registeredService)) {
            val signingKey = property.getPropertyValue(registeredService).getValue();
            return Optional.of(signingKey);
        }
        return Optional.empty();
    }

    /**
     * Gets encryption key.
     *
     * @param registeredService the registered service
     * @return the encryption key
     */
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        val property = getEncryptionKeyRegisteredServiceProperty();
        if (property.isAssignedTo(registeredService)) {
            val key = property.getPropertyValue(registeredService).getValue();
            return Optional.of(key);
        }
        return Optional.empty();
    }

    /**
     * Gets cipher operations registered service property.
     *
     * @param registeredService the registered service
     * @return the cipher operations order registered service property
     */
    protected RegisteredServiceProperties getCipherStrategyTypeRegisteredServiceProperty(final RegisteredService registeredService) {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_CIPHER_STRATEGY_TYPE;
    }


    /**
     * Gets signing key registered service property.
     *
     * @return the signing key registered service property
     */
    protected RegisteredServiceProperties getSigningKeyRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY;
    }

    /**
     * Gets encryption key registered service property.
     *
     * @return the encryption key registered service property
     */
    protected RegisteredServiceProperties getEncryptionKeyRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY;
    }
}
