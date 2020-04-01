package org.apereo.cas.oidc.jwks;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link OidcDefaultJsonWebKeystoreCacheLoader}.
 * Only attempts to cache the default CAS keystore.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcDefaultJsonWebKeystoreCacheLoader implements CacheLoader<String, Optional<PublicJsonWebKey>> {
    private final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService;

    private static PublicJsonWebKey getJsonSigningWebKeyFromJwks(final JsonWebKeySet jwks) {
        if (jwks.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys are available in the keystore");
            return null;
        }

        val key = PublicJsonWebKey.class.cast(jwks.getJsonWebKeys().get(0));
        if (StringUtils.isBlank(key.getAlgorithm())) {
            LOGGER.debug("Located JSON web key [{}] has no algorithm defined", key);
        }
        if (StringUtils.isBlank(key.getKeyId())) {
            LOGGER.debug("Located JSON web key [{}] has no key id defined", key);
        }

        if (key.getPrivateKey() == null) {
            LOGGER.warn("Located JSON web key [{}] has no private key", key);
            return null;
        }
        return key;
    }

    private static JsonWebKeySet buildJsonWebKeySet(final Resource resource) throws Exception {
        val json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
        return buildJsonWebKeySet(json);
    }

    private static JsonWebKeySet buildJsonWebKeySet(final String json) throws Exception {
        val jsonWebKeySet = new JsonWebKeySet(json);
        val webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
        if (webKey == null || webKey.getPrivateKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] is not found or has no associated private key", webKey);
            return null;
        }
        return jsonWebKeySet;
    }

    /**
     * Build json web key set.
     *
     * @return the json web key set
     */
    private Optional<JsonWebKeySet> buildJsonWebKeySet() {
        try {
            val jwksFile = oidcJsonWebKeystoreGeneratorService.generate();
            LOGGER.debug("Loading default JSON web key from [{}]", jwksFile);
            if (jwksFile == null) {
                return Optional.empty();
            }
            LOGGER.trace("Retrieving default JSON web key from [{}]", jwksFile);
            val jsonWebKeySet = buildJsonWebKeySet(jwksFile);

            if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
                LOGGER.warn("No JSON web keys could be found");
                return Optional.empty();
            }
            val badKeysCount = jsonWebKeySet.getJsonWebKeys().stream().filter(k ->
                StringUtils.isBlank(k.getAlgorithm())
                    && StringUtils.isBlank(k.getKeyId())
                    && StringUtils.isBlank(k.getKeyType())).count();

            if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
                LOGGER.warn("No valid JSON web keys could be found");
                return Optional.empty();
            }

            val webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
            if (webKey != null && webKey.getPrivateKey() == null) {
                LOGGER.warn("JSON web key retrieved [{}] has no associated private key", webKey.getKeyId());
                return Optional.empty();
            }
            LOGGER.trace("Loaded JSON web key set as [{}]", jsonWebKeySet.toJson());
            return Optional.of(jsonWebKeySet);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PublicJsonWebKey> load(final String issuer) {
        val jwks = buildJsonWebKeySet();
        if (jwks.isEmpty() || jwks.get().getJsonWebKeys().isEmpty()) {
            return Optional.empty();
        }
        val key = getJsonSigningWebKeyFromJwks(jwks.get());
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(key);
    }
}
