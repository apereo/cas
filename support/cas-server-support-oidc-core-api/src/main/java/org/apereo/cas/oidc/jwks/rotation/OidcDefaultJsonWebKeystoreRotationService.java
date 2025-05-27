package org.apereo.cas.oidc.jwks.rotation;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.io.Resource;
import java.util.Optional;

/**
 * This is {@link OidcDefaultJsonWebKeystoreRotationService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcDefaultJsonWebKeystoreRotationService implements OidcJsonWebKeystoreRotationService {
    private final OidcProperties oidcProperties;

    private final OidcJsonWebKeystoreGeneratorService generatorService;

    @Override
    public JsonWebKeySet rotate() {
        return whenKeystoreResourceExists()
            .map(Unchecked.function(resource -> {
                LOGGER.trace("Rotating keys found in [{}]", resource);
                val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource);

                jsonWebKeySet.getJsonWebKeys().forEach(key -> {
                    LOGGER.debug("Processing key [{}] to determine rotation eligibility", key.getKeyId());

                    val state = JsonWebKeyLifecycleStates.getJsonWebKeyState(key);
                    if (state == JsonWebKeyLifecycleStates.CURRENT) {
                        JsonWebKeyLifecycleStates.setJsonWebKeyState(key, JsonWebKeyLifecycleStates.PREVIOUS);
                        LOGGER.trace("Rotating state for current key [{}] to previous", key.getKeyId());
                    }
                    if (state == JsonWebKeyLifecycleStates.FUTURE) {
                        JsonWebKeyLifecycleStates.setJsonWebKeyState(key, JsonWebKeyLifecycleStates.CURRENT);
                        LOGGER.trace("Rotating state for future key [{}] to current", key.getKeyId());
                    }
                });
                generateFutureKeys(jsonWebKeySet);
                generateCurrentKeys(jsonWebKeySet);

                return generatorService.store(jsonWebKeySet);
            }))
            .orElse(null);
    }

    private void generateCurrentKeys(final JsonWebKeySet jsonWebKeySet) {
        val foundCurrent = jsonWebKeySet.getJsonWebKeys()
            .stream().anyMatch(key -> JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isCurrent());
        if (!foundCurrent) {
            val currentKeySigning = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties, OidcJsonWebKeyUsage.SIGNING);
            JsonWebKeyLifecycleStates.setJsonWebKeyState(currentKeySigning, JsonWebKeyLifecycleStates.CURRENT);
            LOGGER.trace("Generated current signing key with id [{}]", currentKeySigning.getKeyId());
            jsonWebKeySet.addJsonWebKey(currentKeySigning);

            val currentKeyEncryption = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties, OidcJsonWebKeyUsage.ENCRYPTION);
            JsonWebKeyLifecycleStates.setJsonWebKeyState(currentKeyEncryption, JsonWebKeyLifecycleStates.CURRENT);
            LOGGER.trace("Generated current encryption key with id [{}]", currentKeyEncryption.getKeyId());
            jsonWebKeySet.addJsonWebKey(currentKeyEncryption);
        }
    }

    private void generateFutureKeys(final JsonWebKeySet jsonWebKeySet) {
        val futureKeySigning = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties, OidcJsonWebKeyUsage.SIGNING);
        JsonWebKeyLifecycleStates.setJsonWebKeyState(futureKeySigning, JsonWebKeyLifecycleStates.FUTURE);
        LOGGER.trace("Generated future signing key with id [{}]", futureKeySigning.getKeyId());
        jsonWebKeySet.addJsonWebKey(futureKeySigning);

        val futureKeyEncryption = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties, OidcJsonWebKeyUsage.ENCRYPTION);
        JsonWebKeyLifecycleStates.setJsonWebKeyState(futureKeyEncryption, JsonWebKeyLifecycleStates.FUTURE);
        LOGGER.trace("Generated future encryption key with id [{}]", futureKeyEncryption.getKeyId());
        jsonWebKeySet.addJsonWebKey(futureKeyEncryption);
    }

    @Override
    public JsonWebKeySet revoke() {
        return whenKeystoreResourceExists()
            .map(Unchecked.function(resource -> {
                LOGGER.trace("Revoking previous keys found in [{}]", resource);
                val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource);
                jsonWebKeySet.getJsonWebKeys().removeIf(key -> {
                    LOGGER.debug("Processing key [{}] to determine revocation eligibility", key.getKeyId());
                    val state = JsonWebKeyLifecycleStates.getJsonWebKeyState(key);
                    return state == JsonWebKeyLifecycleStates.PREVIOUS;
                });
                return generatorService.store(jsonWebKeySet);
            }))
            .orElse(null);
    }

    private Optional<Resource> whenKeystoreResourceExists() {
        return FunctionUtils.doUnchecked(generatorService::find);
    }
}
