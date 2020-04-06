package org.apereo.cas.oidc.jwks;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.ResourceUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.keys.EllipticCurves;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link OidcJsonWebKeyStoreUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
@Slf4j
public class OidcJsonWebKeyStoreUtils {
    private static final int JWK_EC_P384_SIZE = 384;

    private static final int JWK_EC_P512_SIZE = 512;

    /**
     * Gets json web key set.
     *
     * @param service        the service
     * @param resourceLoader the resource loader
     * @return the json web key set
     */
    public static Optional<JsonWebKeySet> getJsonWebKeySet(final OidcRegisteredService service,
                                                           final ResourceLoader resourceLoader) {
        try {
            LOGGER.trace("Loading JSON web key from [{}]", service.getJwks());

            val resource = getJsonWebKeySetResource(service, resourceLoader);
            if (resource == null) {
                LOGGER.warn("No JSON web keys or keystore resource could be found for [{}]", service);
                return Optional.empty();
            }
            val jsonWebKeySet = buildJsonWebKeySet(resource);

            if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
                LOGGER.warn("No JSON web keys could be found for [{}]", service);
                return Optional.empty();
            }

            val badKeysCount = jsonWebKeySet.getJsonWebKeys().stream().filter(k ->
                StringUtils.isBlank(k.getAlgorithm())
                    && StringUtils.isBlank(k.getKeyId())
                    && StringUtils.isBlank(k.getKeyType())).count();

            if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
                LOGGER.warn("No valid JSON web keys could be found for [{}]", service);
                return Optional.empty();
            }

            val webKey = getJsonWebKeyFromJsonWebKeySet(jsonWebKeySet);
            if (Objects.requireNonNull(webKey).getPublicKey() == null) {
                LOGGER.warn("JSON web key retrieved [{}] has no associated public key", webKey.getKeyId());
                return Optional.empty();
            }
            return Optional.of(jsonWebKeySet);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Gets json web key from jwks.
     *
     * @param jwks the jwks
     * @return the json web key from jwks
     */
    public static PublicJsonWebKey getJsonWebKeyFromJsonWebKeySet(final JsonWebKeySet jwks) {
        if (jwks.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys are available in the keystore");
            return null;
        }

        val key = (PublicJsonWebKey) jwks.getJsonWebKeys().get(0);
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

    private static JsonWebKeySet buildJsonWebKeySet(final Resource resource) throws Exception {
        LOGGER.debug("Loading JSON web key from [{}]", resource);
        val json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
        return buildJsonWebKeySet(json);
    }

    private static JsonWebKeySet buildJsonWebKeySet(final String json) throws Exception {
        val jsonWebKeySet = new JsonWebKeySet(json);
        val webKey = getJsonWebKeyFromJsonWebKeySet(jsonWebKeySet);
        if (webKey == null || webKey.getPublicKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] is not found or has no associated public key", webKey);
            return null;
        }
        return jsonWebKeySet;
    }

    private static Resource getJsonWebKeySetResource(final OidcRegisteredService service,
                                                     final ResourceLoader resourceLoader) {
        if (StringUtils.isNotBlank(service.getJwks())) {
            if (ResourceUtils.doesResourceExist(service.getJwks())) {
                return resourceLoader.getResource(service.getJwks());
            }
            return new InputStreamResource(new ByteArrayInputStream(service.getJwks().getBytes(StandardCharsets.UTF_8)), "JWKS");
        }
        return null;
    }

    /**
     * Parse json web key set json web key set.
     *
     * @param json the json
     * @return the json web key set
     */
    @SneakyThrows
    public static JsonWebKeySet parseJsonWebKeySet(final String json) {
        return new JsonWebKeySet(json);
    }

    /**
     * Generate json web key public.
     *
     * @param jwksType    the jwks type
     * @param jwksKeySize the jwks key size
     * @return the public json web key
     */
    @SneakyThrows
    public static PublicJsonWebKey generateJsonWebKey(final String jwksType, final int jwksKeySize) {
        switch (jwksType.trim().toLowerCase()) {
            case "ec":
                if (jwksKeySize == JWK_EC_P384_SIZE) {
                    val jwk = EcJwkGenerator.generateJwk(EllipticCurves.P384);
                    jwk.setKeyId(UUID.randomUUID().toString());
                    jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384);
                    return jwk;
                }
                if (jwksKeySize == JWK_EC_P512_SIZE) {
                    val jwk = EcJwkGenerator.generateJwk(EllipticCurves.P521);
                    jwk.setKeyId(UUID.randomUUID().toString());
                    jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
                    return jwk;
                }
                val jwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
                jwk.setKeyId(UUID.randomUUID().toString());
                jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
                return jwk;
            case "rsa":
            default:
                return RsaJwkGenerator.generateJwk(jwksKeySize);
        }
    }
}
