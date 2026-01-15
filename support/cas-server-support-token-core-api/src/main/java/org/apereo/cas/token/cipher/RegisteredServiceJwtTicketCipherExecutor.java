package org.apereo.cas.token.cipher;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
        return getSigningKey(registeredService).isPresent() || getEncryptionKey(registeredService).isPresent();
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
        val cipher = createCipherExecutorInstance(encryptionKey, signingKey, registeredService);
        val order = getCipherOperationsStrategyType(registeredService).orElse(CipherOperationsStrategyType.ENCRYPT_AND_SIGN);
        cipher.setStrategyType(order);
        cipher.setSigningEnabled(isSigningEnabledForRegisteredService(registeredService));
        cipher.setEncryptionEnabled(isEncryptionEnabledForRegisteredService(registeredService));
        return cipher;
    }


    protected JwtTicketCipherExecutor createCipherExecutorInstance(final String encryptionKey, final String signingKey,
                                                                   final RegisteredService registeredService) {
        val cipher = new JwtTicketCipherExecutor(encryptionKey, signingKey,
            StringUtils.isNotBlank(encryptionKey), StringUtils.isNotBlank(signingKey), 0, 0);

        val encryptionAlg = getEncryptionAlgRegisteredServiceProperty().isAssignedTo(registeredService)
            ? getEncryptionAlgRegisteredServiceProperty().getPropertyValue(registeredService).value()
            : EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM;
        cipher.setContentEncryptionAlgorithmIdentifier(encryptionAlg);
        cipher.getCommonHeaders().putAll(CollectionUtils.wrap(CUSTOM_HEADER_REGISTERED_SERVICE_ID, registeredService.getId()));
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
            val signingKey = property.getPropertyValue(registeredService).value();
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
            val key = property.getPropertyValue(registeredService).value();
            return Optional.of(key);
        }
        return Optional.empty();
    }

    /**
     * Is signing enabled for registered service ?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    protected boolean isSigningEnabledForRegisteredService(final RegisteredService registeredService) {
        val prop = getCipherOperationRegisteredServiceSigningEnabledProperty();
        if (prop.isAssignedTo(registeredService)) {
            return prop.getPropertyBooleanValue(registeredService);
        }
        return BooleanUtils.toBoolean(prop.getDefaultValue());
    }

    /**
     * Is encryption enabled for registered service ?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    protected boolean isEncryptionEnabledForRegisteredService(final RegisteredService registeredService) {
        val prop = getCipherOperationRegisteredServiceEncryptionEnabledProperty();
        if (prop.isAssignedTo(registeredService)) {
            return prop.getPropertyBooleanValue(registeredService);
        }
        return BooleanUtils.toBoolean(prop.getDefaultValue());
    }

    protected RegisteredServiceProperties getSigningKeyRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY;
    }

    protected RegisteredServiceProperties getEncryptionKeyRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY;
    }

    protected RegisteredServiceProperties getEncryptionAlgRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_ALG;
    }

    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceSigningEnabledProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_ENABLED;
    }

    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceEncryptionEnabledProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_ENABLED;
    }
}
