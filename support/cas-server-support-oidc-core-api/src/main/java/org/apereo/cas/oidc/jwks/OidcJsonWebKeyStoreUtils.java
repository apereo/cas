package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtCipherExecutor;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.JsonUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.EllipticCurves;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.spec.ECParameterSpec;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
     * @param oidcRegisteredService        the service
     * @param resourceLoader the resource loader
     * @param usage          the usage
     * @return the json web key set
     */
    public static Optional<JsonWebKeySet> getJsonWebKeySet(final OidcRegisteredService oidcRegisteredService,
                                                           final ResourceLoader resourceLoader,
                                                           final Optional<OidcJsonWebKeyUsage> usage) {
        return FunctionUtils.doAndHandle(
                () -> {
                    val serviceJwks = SpringExpressionLanguageValueResolver.getInstance().resolve(oidcRegisteredService.getJwks());
                    LOGGER.trace("Loading JSON web key from [{}]", serviceJwks);
                    val resource = getJsonWebKeySetResource(oidcRegisteredService, resourceLoader);
                    if (resource == null) {
                        LOGGER.debug("No JSON web keys or keystore resource could be found for [{}]", oidcRegisteredService.getServiceId());
                        return Optional.empty();
                    }
                    val requestedKid = Optional.ofNullable(oidcRegisteredService.getJwksKeyId());
                    return buildJsonWebKeySet(resource, requestedKid, usage);
                },
                (CheckedFunction<Throwable, Optional<JsonWebKeySet>>) throwable -> {
                    LoggingUtils.error(LOGGER, throwable);
                    return Optional.empty();
                })
            .get();
    }

    /**
     * Gets json web key from jwks.
     *
     * @param jwks         the jwks
     * @param requestedKey the kid
     * @param usage        the usage
     * @return the json web key from jwks
     */
    public static Optional<JsonWebKeySet> getJsonWebKeyFromJsonWebKeySet(
        final JsonWebKeySet jwks, final Optional<String> requestedKey, final Optional<OidcJsonWebKeyUsage> usage) {
        if (jwks.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys are available in the keystore");
            return Optional.empty();
        }

        val keyResult = getJsonWebKeyByKeyId(jwks, requestedKey, usage)
            .getJsonWebKeys()
            .stream()
            .filter(key -> key.getKey() != null)
            .collect(Collectors.toList());
        if (keyResult.isEmpty()) {
            LOGGER.warn("Unable to locate JSON web key for [{}]", requestedKey.map(Object::toString));
            return Optional.empty();
        }
        return Optional.of(new JsonWebKeySet(keyResult));
    }

    private static List<JsonWebKey> filterJsonWebKeySetKeysBy(final JsonWebKeySet jwks,
                                                              final Optional<String> keyIdRequest,
                                                              final Optional<OidcJsonWebKeyUsage> usage) {

        var filter = (Predicate<JsonWebKey>) PublicJsonWebKey.class::isInstance;
        if (keyIdRequest.isPresent()) {
            filter = filter.and(jsonWebKey -> StringUtils.equalsIgnoreCase(jsonWebKey.getKeyId(), keyIdRequest.get()));
        }
        if (usage.isPresent()) {
            filter = filter.and(jsonWebKey -> usage.get().is(jsonWebKey));
        }
        return jwks.getJsonWebKeys()
            .stream()
            .filter(filter)
            .map(PublicJsonWebKey.class::cast)
            .collect(Collectors.toList());
    }

    private static JsonWebKeySet getJsonWebKeyByKeyId(final JsonWebKeySet jwks,
                                                      final Optional<String> kid,
                                                      final Optional<OidcJsonWebKeyUsage> usage) {
        if (kid.isPresent()) {
            var resultJwks = filterJsonWebKeySetKeysBy(jwks, kid, usage);
            if (usage.isPresent() && resultJwks.isEmpty()) {
                LOGGER.debug("No JSON web keys found for [{}] and usage [{}]. Skipping usage...", kid.get(), usage.get());
                resultJwks = filterJsonWebKeySetKeysBy(jwks, kid, Optional.empty());
            }
            LOGGER.debug("JSON web keys found for [{}] are [{}]", kid.get(), resultJwks);
            return new JsonWebKeySet(resultJwks);
        }
        var resultJwks = filterJsonWebKeySetKeysBy(jwks, Optional.empty(), usage);
        if (usage.isPresent() && resultJwks.isEmpty()) {
            LOGGER.debug("No JSON web keys found for usage [{}]. Skipping usage...", usage.get());
            resultJwks = filterJsonWebKeySetKeysBy(jwks, Optional.empty(), Optional.empty());
        }
        LOGGER.debug("JSON web keys found are [{}]", resultJwks);
        return new JsonWebKeySet(resultJwks);
    }

    private static Optional<JsonWebKeySet> buildJsonWebKeySet(final Resource resource,
                                                              final Optional<String> keyId,
                                                              final Optional<OidcJsonWebKeyUsage> usage) throws Exception {
        LOGGER.debug("Loading JSON web key from [{}]", resource);
        try (val is = resource.getInputStream()) {
            val json = IOUtils.toString(is, StandardCharsets.UTF_8);
            LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
            return buildJsonWebKeySet(json, keyId, usage);
        }
    }

    private Optional<JsonWebKeySet> buildJsonWebKeySet(
        final String json, final Optional<String> keyId,
        final Optional<OidcJsonWebKeyUsage> usage) throws Exception {
        if (JsonUtils.isValidJson(json)) {
            return getJsonWebKeyFromJsonWebKeySet(new JsonWebKeySet(json), keyId, usage);
        }
        val key = new AesKey(json.getBytes(StandardCharsets.UTF_8));
        val jsonWebKey = new OctetSequenceJsonWebKey(key);
        jsonWebKey.setKeyId(keyId.orElse(StringUtils.EMPTY));
        jsonWebKey.setUse(usage.map(Enum::name).orElse(StringUtils.EMPTY));
        return Optional.of(new JsonWebKeySet(jsonWebKey));
    }

    private static Resource getJsonWebKeySetResource(final OidcRegisteredService service,
                                                     final ResourceLoader resourceLoader) {
        val serviceJwks = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getJwks());
        if (StringUtils.isNotBlank(serviceJwks)) {
            if (ResourceUtils.doesResourceExist(serviceJwks)) {
                return resourceLoader.getResource(serviceJwks);
            }
            return new InputStreamResource(new ByteArrayInputStream(serviceJwks.getBytes(StandardCharsets.UTF_8)), "JWKS");
        }
        return null;
    }

    /**
     * Parse json web key set.
     *
     * @param json the json
     * @return the json web key set
     */
    public static JsonWebKeySet parseJsonWebKeySet(final String json) {
        return FunctionUtils.doUnchecked(() -> new JsonWebKeySet(json));
    }

    private static PublicJsonWebKey generateJsonWebKeyEC(final ECParameterSpec spec) {
        return FunctionUtils.doUnchecked(() -> EcJwkGenerator.generateJwk(spec));
    }

    /**
     * Generate json web key.
     *
     * @param jwksType    the jwks type
     * @param jwksKeySize the jwks key size
     * @param usage       the usage
     * @return the public json web key
     */
    public static PublicJsonWebKey generateJsonWebKey(final String jwksType, final int jwksKeySize,
                                                      final OidcJsonWebKeyUsage usage) {
        switch (jwksType.trim().toLowerCase(Locale.ENGLISH)) {
            case "ec" -> {
                if (jwksKeySize == JWK_EC_P384_SIZE) {
                    val jwk = generateJsonWebKeyEC(EllipticCurves.P384);
                    jwk.setKeyId(UUID.randomUUID().toString());
                    jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384);
                    usage.assignTo(jwk);
                    return jwk;
                }
                if (jwksKeySize == JWK_EC_P512_SIZE) {
                    val jwk = generateJsonWebKeyEC(EllipticCurves.P521);
                    jwk.setKeyId(UUID.randomUUID().toString());
                    jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
                    usage.assignTo(jwk);
                    return jwk;
                }
                val jwk = generateJsonWebKeyEC(EllipticCurves.P256);
                jwk.setKeyId(UUID.randomUUID().toString());
                jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
                usage.assignTo(jwk);
                return jwk;
            }
            default -> {
                val newJwk = FunctionUtils.doUnchecked(() -> RsaJwkGenerator.generateJwk(jwksKeySize));
                newJwk.setKeyId(UUID.randomUUID().toString());
                usage.assignTo(newJwk);
                return newJwk;
            }
        }
    }


    /**
     * Fetch json web key set for signing operations.
     *
     * @param registeredService the registered service
     * @param cipherExecutor    the cipher executor
     * @param fallbackToDefault the fallback to default
     * @return the optional
     */
    public static Optional<JsonWebKeySet> fetchJsonWebKeySetForSigning(
        final RegisteredService registeredService,
        final OidcRegisteredServiceJwtCipherExecutor cipherExecutor,
        final boolean fallbackToDefault) {
        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        val issuer = cipherExecutor.getOidcIssuerService().determineIssuer(Optional.of(oidcRegisteredService));
        LOGGER.trace("Using issuer [{}] to determine JWKS from default keystore cache", issuer);
        val serviceJsonWebKeys = Objects.requireNonNull(cipherExecutor.getRegisteredServiceJsonWebKeystoreCache().get(
            new OidcJsonWebKeyCacheKey(oidcRegisteredService, OidcJsonWebKeyUsage.SIGNING)));
        if (serviceJsonWebKeys.isPresent()) {
            val jsonWebKey = serviceJsonWebKeys.get();
            LOGGER.debug("Found JSON web key to sign the token: [{}]", jsonWebKey);
            val keys = jsonWebKey.getJsonWebKeys().stream()
                .filter(key -> key.getKey() != null).collect(Collectors.toList());
            return Optional.of(new JsonWebKeySet(keys));
        }
        if (fallbackToDefault) {
            val cacheKey = new OidcJsonWebKeyCacheKey(issuer, OidcJsonWebKeyUsage.SIGNING);
            val defaultJsonWebKeys = cipherExecutor.getDefaultJsonWebKeystoreCache().get(cacheKey);
            if (defaultJsonWebKeys != null) {
                return Optional.of(defaultJsonWebKeys);
            }
        }
        LOGGER.warn("No [{}] key could be found for issuer [{}]", OidcJsonWebKeyUsage.SIGNING, issuer);
        return Optional.empty();
    }

    /**
     * Fetch json web key set for encryption.
     *
     * @param registeredService the registered service
     * @param cipherExecutor    the cipher executor
     * @return the optional
     */
    public static Optional<JsonWebKeySet> fetchJsonWebKeySetForEncryption(final RegisteredService registeredService,
                                                                          final OidcRegisteredServiceJwtCipherExecutor cipherExecutor) {
        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        val jwks = Objects.requireNonNull(cipherExecutor.getRegisteredServiceJsonWebKeystoreCache().get(
            new OidcJsonWebKeyCacheKey(oidcRegisteredService, OidcJsonWebKeyUsage.ENCRYPTION)));
        if (jwks.isEmpty()) {
            LOGGER.warn("Service [{}] with client id [{}] is configured to encrypt tokens, yet no JSON web key is available",
                oidcRegisteredService.getServiceId(), oidcRegisteredService.getClientId());
            return Optional.empty();
        }
        val jsonWebKey = jwks.get();
        LOGGER.debug("Found JSON web key to encrypt the token: [{}]", jsonWebKey);

        val keys = jsonWebKey.getJsonWebKeys().stream().filter(key -> key.getKey() != null).toList();
        if (keys.isEmpty()) {
            LOGGER.warn("No valid JSON web keys used for encryption can be found");
            return Optional.empty();
        }
        return Optional.of(new JsonWebKeySet(keys));
    }
}
