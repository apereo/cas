package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link DefaultOAuth20ClientSecretValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultOAuth20ClientSecretValidator implements OAuth20ClientSecretValidator {
    private final ObjectProvider<OAuth20ConfigurationContext> configurationContext;
    private final CipherExecutor<Serializable, String> cipherExecutor;

    @Override
    public boolean validate(final OAuthRegisteredService registeredService, final String clientSecret) {
        if (isClientSecretUndefined(registeredService)) {
            LOGGER.debug("The client secret is not defined for the registered service [{}]", registeredService.getName());
            return true;
        }

        val secretsByExpiration = registeredService.getClientSecrets()
            .stream()
            .collect(Collectors.partitioningBy(
                secret -> isClientSecretExpired(secret, registeredService),
                Collectors.toList()));

        val expiredSecrets = secretsByExpiration.getOrDefault(Boolean.TRUE, List.of());
        if (!expiredSecrets.isEmpty()) {
            expiredSecrets.forEach(secret -> registeredService.getClientSecrets().remove(secret));
            configurationContext.getObject().getServicesManager().save(registeredService);
        }
        
        val candidateSecrets = secretsByExpiration.getOrDefault(Boolean.FALSE, List.of());

        val secretIsValid = candidateSecrets
            .stream()
            .map(secret -> SpringExpressionLanguageValueResolver.getInstance().resolve(secret.getValue()))
            .map(secretValue -> cipherExecutor.decode(secretValue, new Object[]{registeredService}))
            .filter(Objects::nonNull)
            .anyMatch(secret -> isClientSecretCorrect(secret, clientSecret));

        if (!secretIsValid) {
            LOGGER.error("Wrong client secret for service: [{}]. If you intend to use PKCE, note that it does not require a client secret and "
                    + "requests generally must not specify a client secret to CAS.\nFurthermore, you must make sure "
                    + "no client secret is assigned to this registered service in the CAS service registry.",
                registeredService.getServiceId());
            return false;
        }
        return true;
    }

    @Override
    public boolean isClientSecretExpired(final OAuthRegisteredServiceClientSecret secret,
                                         final OAuthRegisteredService registeredService) {
        return secret.hasClientSecretExpired(registeredService);
    }

    protected boolean isClientSecretUndefined(final OAuthRegisteredService registeredService) {
        return registeredService != null
            && (registeredService.getClientSecrets() == null || registeredService.getClientSecrets().isEmpty());
    }

    protected boolean isClientSecretCorrect(@Nullable final String definedSecret, final String clientSecret) {
        return Strings.CI.equals(definedSecret, clientSecret);
    }
}
