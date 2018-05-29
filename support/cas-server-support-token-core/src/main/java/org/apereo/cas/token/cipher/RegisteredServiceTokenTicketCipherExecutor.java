package org.apereo.cas.token.cipher;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;

import java.util.Optional;

/**
 * This is {@link RegisteredServiceTokenTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@NoArgsConstructor
public class RegisteredServiceTokenTicketCipherExecutor extends TokenTicketCipherExecutor implements RegisteredServiceCipherExecutor {

    @Override
    public String decode(final String data, final Optional<RegisteredService> service) {
        if (service.isPresent()) {
            final var registeredService = service.get();
            if (supports(registeredService)) {
                LOGGER.debug("Found signing and/or encryption keys for [{}] in service registry to decode", registeredService.getServiceId());
                final var encryptionKey = getEncryptionKey(registeredService).get();
                final var signingKey = getSigningKey(registeredService).get();
                final var cipher = new TokenTicketCipherExecutor(encryptionKey, signingKey,
                    StringUtils.isNotBlank(encryptionKey), StringUtils.isNotBlank(signingKey));
                if (cipher.isEnabled()) {
                    return cipher.decode(data);
                }
            }
        }
        return decode(data);
    }

    @Override
    public String encode(final String data, final Optional<RegisteredService> service) {
        if (service.isPresent()) {
            final var registeredService = service.get();
            if (supports(registeredService)) {
                LOGGER.debug("Found signing and/or encryption keys for [{}] in service registry to encode", registeredService.getServiceId());
                final var encryptionKey = getEncryptionKey(registeredService).get();
                final var signingKey = getSigningKey(registeredService).get();
                final var cipher = new TokenTicketCipherExecutor(encryptionKey, signingKey,
                    StringUtils.isNotBlank(encryptionKey), StringUtils.isNotBlank(signingKey));
                if (cipher.isEnabled()) {
                    return cipher.encode(data);
                }
            }
        }
        return encode(data);
    }

    @Override
    public boolean supports(final RegisteredService registeredService) {
        return getSigningKey(registeredService).isPresent() || getEncryptionKey(registeredService).isPresent();
    }

    /**
     * Gets signing key.
     *
     * @param registeredService the registered service
     * @return the signing key
     */
    public Optional<String> getSigningKey(final RegisteredService registeredService) {
        if (RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.isAssignedTo(registeredService)) {
            final var signingKey = RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyValue(registeredService).getValue();
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
        if (RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY.isAssignedTo(registeredService)) {
            final var key = RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY.getPropertyValue(registeredService).getValue();
            return Optional.of(key);
        }
        return Optional.empty();
    }
}
