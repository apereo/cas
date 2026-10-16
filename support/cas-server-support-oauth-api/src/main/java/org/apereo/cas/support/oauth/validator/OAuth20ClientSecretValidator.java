package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
     * Determines whether the client secret is expired.
     *
     * @param secret            the secret
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isClientSecretExpired(OAuthRegisteredServiceClientSecret secret, OAuthRegisteredService registeredService);

    /**
     * Remove the expired client secrets of the registered service. When all of them have expired, the one that expired
     * last is kept, so that the service is never left without a client secret and taken for a public client.
     * The list of client secrets is replaced rather than modified, since requests may be reading it.
     *
     * @param registeredService the registered service
     * @return true if any client secret was removed
     */
    default boolean removeExpiredClientSecrets(final OAuthRegisteredService registeredService) {
        val clientSecrets = registeredService.getClientSecrets();
        if (clientSecrets == null || clientSecrets.isEmpty()) {
            return false;
        }
        val secretsByExpiration = clientSecrets
            .stream()
            .collect(Collectors.partitioningBy(secret -> isClientSecretExpired(secret, registeredService)));
        val remainingSecrets = new ArrayList<>(secretsByExpiration.get(Boolean.FALSE));
        if (remainingSecrets.isEmpty()) {
            val expiredSecrets = secretsByExpiration.get(Boolean.TRUE);
            remainingSecrets.add(expiredSecrets
                .stream()
                .filter(secret -> StringUtils.isNotBlank(secret.getExpiration()))
                .max(Comparator.comparing(OAuthRegisteredServiceClientSecret::toEffectiveExpiration))
                .orElseGet(expiredSecrets::getLast));
        }
        if (remainingSecrets.size() == clientSecrets.size()) {
            return false;
        }
        registeredService.setClientSecrets(remainingSecrets);
        return true;
    }
}
