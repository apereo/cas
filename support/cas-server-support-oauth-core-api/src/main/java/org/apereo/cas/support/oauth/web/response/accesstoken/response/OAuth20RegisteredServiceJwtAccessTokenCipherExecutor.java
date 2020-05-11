package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Optional;

/**
 * This is {@link OAuth20RegisteredServiceJwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OAuth20RegisteredServiceJwtAccessTokenCipherExecutor extends RegisteredServiceJwtTicketCipherExecutor {
    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getSigningKeyRegisteredServiceProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY;
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getEncryptionKeyRegisteredServiceProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY;
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherStrategyTypeRegisteredServiceProperty(
        final RegisteredService registeredService) {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_CIPHER_STRATEGY_TYPE;
    }

    @Override
    public Optional<String> getSigningKey(final RegisteredService registeredService) {
        if (isSigningEnabledForRegisteredService(registeredService)) {
            return super.getSigningKey(registeredService);
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
        val prop = RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED;
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
        val prop = RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED;
        if (prop.isAssignedTo(registeredService)) {
            return prop.getPropertyBooleanValue(registeredService);
        }
        return BooleanUtils.toBoolean(prop.getDefaultValue());
    }

    @Override
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        if (isEncryptionEnabledForRegisteredService(registeredService)) {
            return super.getEncryptionKey(registeredService);
        }
        return Optional.empty();
    }
}
