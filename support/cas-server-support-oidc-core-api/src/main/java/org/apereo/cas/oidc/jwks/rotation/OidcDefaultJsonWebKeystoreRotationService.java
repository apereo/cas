package org.apereo.cas.oidc.jwks.rotation;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
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
    public JsonWebKeySet rotate() throws Exception {
        return whenKeystoreResourceExists()
            .map(Unchecked.function(resource -> {
                LOGGER.trace("Rotating keys found in [{}]", resource);
                val jwksJson = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                val jsonWebKeySet = new JsonWebKeySet(jwksJson);

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
                val generatedKey = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties);
                JsonWebKeyLifecycleStates.setJsonWebKeyState(generatedKey, JsonWebKeyLifecycleStates.FUTURE);
                LOGGER.trace("Generated future key with id [{}]", generatedKey.getKeyId());

                val foundCurrent = jsonWebKeySet.getJsonWebKeys()
                    .stream().anyMatch(key -> JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isCurrent());
                if (!foundCurrent) {
                    val currentKey = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties);
                    JsonWebKeyLifecycleStates.setJsonWebKeyState(currentKey, JsonWebKeyLifecycleStates.CURRENT);
                    LOGGER.trace("Generated current key with id [{}]", currentKey.getKeyId());
                    jsonWebKeySet.addJsonWebKey(currentKey);
                }

                jsonWebKeySet.addJsonWebKey(generatedKey);
                return generatorService.store(jsonWebKeySet);
            }))
            .orElse(null);
    }

    @Override
    public JsonWebKeySet revoke() throws Exception {
        return whenKeystoreResourceExists()
            .map(Unchecked.function(resource -> {
                LOGGER.trace("Revoking previous keys found in [{}]", resource);
                val jwksJson = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                val jsonWebKeySet = new JsonWebKeySet(jwksJson);
                jsonWebKeySet.getJsonWebKeys().removeIf(key -> {
                    LOGGER.debug("Processing key [{}] to determine revocation eligibility", key.getKeyId());
                    val state = JsonWebKeyLifecycleStates.getJsonWebKeyState(key);
                    return state == JsonWebKeyLifecycleStates.PREVIOUS;
                });
                return generatorService.store(jsonWebKeySet);
            }))
            .orElse(null);
    }

    private Optional<Resource> whenKeystoreResourceExists() throws Exception {
        val resolve = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(oidcProperties.getJwks().getFileSystem().getJwksFile());
        val resource = ResourceUtils.getRawResourceFrom(resolve);
        return Optional.ofNullable(ResourceUtils.doesResourceExist(resource) ? resource : null);
    }
}
