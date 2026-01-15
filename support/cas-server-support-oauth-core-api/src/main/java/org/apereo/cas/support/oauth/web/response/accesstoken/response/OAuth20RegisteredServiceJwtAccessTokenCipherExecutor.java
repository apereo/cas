package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;

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
    protected RegisteredServiceProperty.RegisteredServiceProperties getEncryptionAlgRegisteredServiceProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ALG;
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

    @Override
    protected boolean isEncryptionEnabledForRegisteredService(final RegisteredService registeredService) {
        return super.isEncryptionEnabledForRegisteredService(registeredService);
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceSigningEnabledProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED;
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceEncryptionEnabledProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED;
    }

    @Override
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        if (isEncryptionEnabledForRegisteredService(registeredService)) {
            return super.getEncryptionKey(registeredService);
        }
        return Optional.empty();
    }

    @Override
    protected JwtTicketCipherExecutor createCipherExecutorInstance(final String encryptionKey, final String signingKey,
                                                                   final RegisteredService registeredService) {
        val cipher = super.createCipherExecutorInstance(encryptionKey, signingKey, registeredService);
        return prepareCipherExecutor(cipher, registeredService);
    }

    protected JwtTicketCipherExecutor prepareCipherExecutor(final JwtTicketCipherExecutor cipher,
                                                            final RegisteredService registeredService) {
        if (registeredService instanceof final OAuthRegisteredService oauthRegisteredService) {
            FunctionUtils.doIfNotBlank(oauthRegisteredService.getJwtAccessTokenSigningAlg(), cipher::setSigningAlgorithm);
        }
        return cipher;
    }
}
