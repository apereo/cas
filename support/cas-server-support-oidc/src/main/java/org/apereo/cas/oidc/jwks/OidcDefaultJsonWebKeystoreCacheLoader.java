package org.apereo.cas.oidc.jwks;

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class OidcDefaultJsonWebKeystoreCacheLoader implements CacheLoader<String, Optional<RsaJsonWebKey>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcDefaultJsonWebKeystoreCacheLoader.class);

    private final Resource jwksFile;

    public OidcDefaultJsonWebKeystoreCacheLoader(final Resource jwksFile) {
        this.jwksFile = jwksFile;
    }

    @Override
    public Optional<RsaJsonWebKey> load(final String issuer) throws Exception {
        final Optional<JsonWebKeySet> jwks = buildJsonWebKeySet();
        if (!jwks.isPresent() || jwks.get().getJsonWebKeys().isEmpty()) {
            return Optional.empty();
        }
        final RsaJsonWebKey key = getJsonSigningWebKeyFromJwks(jwks.get());
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(key);
    }
    
    private static RsaJsonWebKey getJsonSigningWebKeyFromJwks(final JsonWebKeySet jwks) {
        if (jwks.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys are available in the keystore");
            return null;
        }

        final RsaJsonWebKey key = (RsaJsonWebKey) jwks.getJsonWebKeys().get(0);
        if (StringUtils.isBlank(key.getAlgorithm())) {
            LOGGER.warn("Located JSON web key [{}] has no algorithm defined", key);
        }
        if (StringUtils.isBlank(key.getKeyId())) {
            LOGGER.warn("Located JSON web key [{}] has no key id defined", key);
        }

        if (key.getPrivateKey() == null) {
            LOGGER.warn("Located JSON web key [{}] has no private key", key);
            return null;
        }
        return key;
    }

    private static JsonWebKeySet buildJsonWebKeySet(final Resource resource) throws Exception {
        final String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
        return buildJsonWebKeySet(json);
    }

    private static JsonWebKeySet buildJsonWebKeySet(final String json) throws Exception {
        final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(json);
        final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
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
            LOGGER.debug("Loading default JSON web key from [{}]", this.jwksFile);
            if (this.jwksFile != null) {
                LOGGER.debug("Retrieving default JSON web key from [{}]", this.jwksFile);
                final JsonWebKeySet jsonWebKeySet = buildJsonWebKeySet(this.jwksFile);

                if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
                    LOGGER.warn("No JSON web keys could be found");
                    return Optional.empty();
                }
                final long badKeysCount = jsonWebKeySet.getJsonWebKeys().stream().filter(k ->
                        StringUtils.isBlank(k.getAlgorithm())
                                && StringUtils.isBlank(k.getKeyId())
                                && StringUtils.isBlank(k.getKeyType())).count();

                if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
                    LOGGER.warn("No valid JSON web keys could be found");
                    return Optional.empty();
                }

                final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
                if (webKey.getPrivateKey() == null) {
                    LOGGER.warn("JSON web key retrieved [{}] has no associated private key", webKey.getKeyId());
                    return Optional.empty();
                }
                return Optional.of(jsonWebKeySet);
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

}
