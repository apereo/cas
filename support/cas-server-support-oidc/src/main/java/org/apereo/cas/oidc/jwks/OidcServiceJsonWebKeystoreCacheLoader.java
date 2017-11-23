package org.apereo.cas.oidc.jwks;

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheLoader}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcServiceJsonWebKeystoreCacheLoader implements CacheLoader<OidcRegisteredService, Optional<RsaJsonWebKey>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcServiceJsonWebKeystoreCacheLoader.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public Optional<RsaJsonWebKey> load(final OidcRegisteredService svc) throws Exception {
        final Optional<JsonWebKeySet> jwks = buildJsonWebKeySet(svc);
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

        if (key.getPublicKey() == null) {
            LOGGER.warn("Located JSON web key [{}] has no public key", key);
            return null;
        }
        return key;
    }

    private Optional<JsonWebKeySet> buildJsonWebKeySet(final OidcRegisteredService service) {
        try {
            LOGGER.debug("Loading JSON web key from [{}]", service.getJwks());
            final Resource resource = this.resourceLoader.getResource(service.getJwks());
            final JsonWebKeySet jsonWebKeySet = buildJsonWebKeySet(resource);

            if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
                LOGGER.warn("No JSON web keys could be found for [{}]", service);
                return Optional.empty();
            }

            final long badKeysCount = jsonWebKeySet.getJsonWebKeys().stream().filter(k ->
                    StringUtils.isBlank(k.getAlgorithm())
                            && StringUtils.isBlank(k.getKeyId())
                            && StringUtils.isBlank(k.getKeyType())).count();

            if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
                LOGGER.warn("No valid JSON web keys could be found for [{}]", service);
                return Optional.empty();
            }

            final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
            if (webKey.getPublicKey() == null) {
                LOGGER.warn("JSON web key retrieved [{}] has no associated public key", webKey.getKeyId());
                return Optional.empty();
            }
            return Optional.of(jsonWebKeySet);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    private static JsonWebKeySet buildJsonWebKeySet(final Resource resource) throws Exception {
        LOGGER.debug("Loading JSON web key from [{}]", resource);
        final String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
        return buildJsonWebKeySet(json);
    }

    private static JsonWebKeySet buildJsonWebKeySet(final String json) throws Exception {
        final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(json);
        final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
        if (webKey == null || webKey.getPublicKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] is not found or has no associated public key", webKey);
            return null;
        }
        return jsonWebKeySet;
    }
}
