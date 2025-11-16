package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.util.LoggingUtils;
import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.Resource;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link OidcDefaultJsonWebKeystoreCacheLoader}.
 * Only attempts to cache the default CAS keystore.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public record OidcDefaultJsonWebKeystoreCacheLoader(OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService)
    implements CacheLoader<@NonNull OidcJsonWebKeyCacheKey, JsonWebKeySet> {
    /**
     * Gets json web key from jwks.
     *
     * @param jwks     the jwks
     * @param cacheKey the cache key
     * @return the json signing web key from jwks
     */
    private static JsonWebKeySet getJsonWebKeysFromJwks(final JsonWebKeySet jwks,
                                                        final OidcJsonWebKeyCacheKey cacheKey) {
        val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeyFromJsonWebKeySet(jwks,
                Optional.empty(), Optional.of(cacheKey.getUsage()))
            .map(JsonWebKeySet::getJsonWebKeys)
            .orElseGet(ArrayList::new);
        return new JsonWebKeySet(keys
            .stream()
            .filter(key -> OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isCurrent())
            .map(PublicJsonWebKey.class::cast)
            .filter(key -> key.getPrivateKey() != null)
            .collect(Collectors.toList()));
    }

    @Override
    public JsonWebKeySet load(final OidcJsonWebKeyCacheKey cacheKey) {
        val jwks = buildJsonWebKeySet(cacheKey);
        if (jwks.isEmpty()) {
            LOGGER.warn("JSON web keystore retrieved is empty for issuer [{}]", cacheKey.getIssuer());
            return null;
        }
        val keySet = jwks.get();
        if (keySet.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("JSON web keystore retrieved [{}] contains no JSON web keys", keySet);
            return null;
        }
        val keys = getJsonWebKeysFromJwks(keySet, cacheKey);
        LOGGER.debug("Found JSON web key as [{}]", keys);
        return keys.getJsonWebKeys().isEmpty() ? null : keys;
    }

    /**
     * Build json web key set.
     *
     * @param resource the resource
     * @param cacheKey the cache key
     * @return the json web key set
     * @throws Throwable the exception
     */
    JsonWebKeySet buildJsonWebKeySet(final Resource resource, final OidcJsonWebKeyCacheKey cacheKey) throws Throwable {
        val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource);
        return getJsonWebKeysFromJwks(jsonWebKeySet, cacheKey);
    }

    /**
     * Build json web key set.
     *
     * @param cacheKey the cache key
     * @return the json web key set
     */
    Optional<JsonWebKeySet> buildJsonWebKeySet(final OidcJsonWebKeyCacheKey cacheKey) {
        try {
            val resource = generateJwksResource();
            if (resource == null) {
                LOGGER.warn("Unable to load or generate a JWKS resource");
                return Optional.empty();
            }
            LOGGER.trace("Retrieving default JSON web key from [{}]", resource);
            val jsonWebKeySet = buildJsonWebKeySet(resource, cacheKey);

            if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
                LOGGER.warn("No JSON web keys could be found");
                return Optional.empty();
            }
            val badKeysCount = jsonWebKeySet.getJsonWebKeys()
                .stream()
                .filter(key ->
                    StringUtils.isBlank(key.getAlgorithm())
                    && StringUtils.isBlank(key.getKeyId())
                    && StringUtils.isBlank(key.getKeyType())).count();

            if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
                LOGGER.warn("No valid JSON web keys could be found. The keys that are found in the keystore "
                            + "do not define an algorithm, key id or key type and cannot be used for JWKS operations.");
                return Optional.empty();
            }
            return Optional.of(jsonWebKeySet);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return Optional.empty();
    }

    /**
     * Generate jwks resource.
     *
     * @return the resource
     * @throws Throwable the exception
     */
    Resource generateJwksResource() throws Throwable {
        val resource = oidcJsonWebKeystoreGeneratorService().generate();
        LOGGER.debug("Loading default JSON web key from [{}]", resource);
        return resource;
    }
}
