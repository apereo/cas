package org.apereo.cas;

import com.google.common.cache.CacheLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.jose4j.jwk.JsonWebKey;
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
 * This is {@link OidcJsonWebKeystoreCacheLoader}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcJsonWebKeystoreCacheLoader extends CacheLoader<OidcRegisteredService, Optional<RsaJsonWebKey>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcJsonWebKeystoreCacheLoader.class);

    @Autowired
    private ResourceLoader resourceLoader;

    private final Resource jwksFile;

    public OidcJsonWebKeystoreCacheLoader(final Resource jwksFile) {
        this.jwksFile = jwksFile;
    }

    @Override
    public Optional<RsaJsonWebKey> load(final OidcRegisteredService s) throws Exception {
        final Optional<JsonWebKeySet> jwks = buildJsonWebKeySet(s);
        if (!jwks.isPresent() || jwks.get().getJsonWebKeys().isEmpty()) {
            return Optional.empty();
        }
        final RsaJsonWebKey key = getJsonSigningWebKeyFromJwks(jwks.get());
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(key);
    }

    /**
     * Build json web key set.
     *
     * @param service the service
     * @return the json web key set
     * @throws Exception the exception
     */
    private Optional<JsonWebKeySet> buildJsonWebKeySet(final OidcRegisteredService service) throws Exception {
        JsonWebKeySet jsonWebKeySet = null;
        try {
            if (StringUtils.isNotBlank(service.getJwks())) {
                LOGGER.debug("Loading JSON web key from [{}]", service.getJwks());
                final Resource resource = this.resourceLoader.getResource(service.getJwks());
                jsonWebKeySet = buildJsonWebKeySet(resource);
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            if (jsonWebKeySet == null) {
                LOGGER.debug("Loading default JSON web key from [{}]", this.jwksFile);
                if (this.jwksFile != null) {
                    LOGGER.debug("Retrieving default JSON web key from [{}]", this.jwksFile);
                    jsonWebKeySet = buildJsonWebKeySet(this.jwksFile);
                }
            }
        }
        if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys could be found for [{}]", service);
            return Optional.empty();
        }

        final long badKeysCount = jsonWebKeySet.getJsonWebKeys().stream().filter(k ->
                StringUtils.isNotBlank(k.getAlgorithm())
                        && StringUtils.isNotBlank(k.getKeyId())
                        && StringUtils.isNotBlank(k.getKeyType())).count();

        if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
            LOGGER.warn("No valid JSON web keys could be found for [{}]", service);
            return Optional.empty();
        }

        final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
        if (webKey.getPrivateKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] has no associated private key", webKey.getKeyId());
            return Optional.empty();
        }
        return Optional.of(jsonWebKeySet);
    }

    private JsonWebKey getJsonWebKey(final OidcRegisteredService svc) {
        try {
            final Optional<JsonWebKeySet> jwks = buildJsonWebKeySet(svc);
            if (jwks.isPresent() && !jwks.get().getJsonWebKeys().isEmpty()) {
                return getJsonSigningWebKeyFromJwks(jwks.get());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;

    }

    private RsaJsonWebKey getJsonSigningWebKeyFromJwks(final JsonWebKeySet jwks) {
        if (jwks.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys are available in the keystore");
            return null;
        }

        final RsaJsonWebKey key = (RsaJsonWebKey) jwks.getJsonWebKeys().get(0);
        if (StringUtils.isNotBlank(key.getAlgorithm())) {
            LOGGER.warn("Located JSON web key [{}] has no algorithm defined", key);
        }
        if (StringUtils.isNotBlank(key.getKeyId())) {
            LOGGER.warn("Located JSON web key [{}] has no key id defined", key);
        }

        if (key.getPrivateKey() == null) {
            LOGGER.warn("Located JSON web key [{}] has no private key", key);
            return null;
        }
        return key;
    }

    private JsonWebKeySet buildJsonWebKeySet(final Resource resource) throws Exception {
        final String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
        return buildJsonWebKeySet(json);
    }

    private JsonWebKeySet buildJsonWebKeySet(final String json) throws Exception {
        final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(json);
        final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
        if (webKey == null || webKey.getPrivateKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] is not found or has no associated private key", webKey);
            return null;
        }
        return jsonWebKeySet;
    }
}
