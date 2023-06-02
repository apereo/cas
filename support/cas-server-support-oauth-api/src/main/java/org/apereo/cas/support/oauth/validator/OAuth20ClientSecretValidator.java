package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.crypto.CipherExecutor;

import java.io.Serializable;

/**
 * This is {@link OAuth20ClientSecretValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface OAuth20ClientSecretValidator {
    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "oauth20ClientSecretValidator";

    /**
     * Gets cipher executor.
     *
     * @return the cipher executor
     */
    CipherExecutor<Serializable, String> getCipherExecutor();

    /**
     * Validate.
     *
     * @param registeredService the registered service
     * @param clientSecret      the client secret
     * @return true/false
     */
    boolean validate(OAuthRegisteredService registeredService, String clientSecret);

    /**
     * Is client secret expired?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isClientSecretExpired(OAuthRegisteredService registeredService);
}
