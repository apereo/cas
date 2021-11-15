package org.apereo.cas.oidc.jwks.rotation;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
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

    private final ConfigurableApplicationContext applicationContext;

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
                storeJsonWebKeys(resource, jsonWebKeySet);
                return jsonWebKeySet;
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
                storeJsonWebKeys(resource, jsonWebKeySet);
                return jsonWebKeySet;
            }))
            .orElse(null);
    }

    private void storeJsonWebKeys(final Resource resource, final JsonWebKeySet jsonWebKeySet) throws IOException {
        if (ResourceUtils.isFile(resource)) {
            val data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            LOGGER.trace("Storing keys in [{}]", resource);
            FileUtils.write(resource.getFile(), data, StandardCharsets.UTF_8);

            LOGGER.debug("Publishing event to broadcast change in [{}]", resource.getFile());
            applicationContext.publishEvent(new OidcJsonWebKeystoreModifiedEvent(this, resource.getFile()));
        }
    }

    private Optional<Resource> whenKeystoreResourceExists() throws Exception {
        val resolve = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(oidcProperties.getJwks().getJwksFile());
        val resource = ResourceUtils.getRawResourceFrom(resolve);
        return Optional.ofNullable(ResourceUtils.doesResourceExist(resource) ? resource : null);
    }
}
