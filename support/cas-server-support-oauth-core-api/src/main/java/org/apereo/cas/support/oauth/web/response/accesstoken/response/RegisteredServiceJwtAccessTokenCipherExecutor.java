package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;

/**
 * This is {@link RegisteredServiceJwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class RegisteredServiceJwtAccessTokenCipherExecutor extends RegisteredServiceJwtTicketCipherExecutor {
    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getSigningKeyRegisteredServiceProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY;
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getEncryptionKeyRegisteredServiceProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY;
    }
}
