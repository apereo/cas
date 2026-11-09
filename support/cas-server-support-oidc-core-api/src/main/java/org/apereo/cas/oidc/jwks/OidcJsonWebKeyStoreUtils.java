package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.dynareg.DefaultOidcClientIdMetadataDocumentFetcher;
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
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

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

    private static final int JWK_EC_P521_SIZE = 521;

    private static final int REMOTE_KEYS_TIMEOUT_MILLIS = 5_000;

    /**
     * Gets json web key set.
     *
     * @param oidcRegisteredService the service
     * @param resourceLoader        the resource loader
     * @param usage                 the usage
     * @return the json web key set
     */
    public static Optional<JsonWebKeySet> getJsonWebKeySet(final OidcRegisteredService oidcRegisteredService,
                                                           final ResourceLoader resourceLoader,
                                                           final Optional<OidcJsonWebKeyUsage> usage) {
        return FunctionUtils.doAndHandle(
                () -> {
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
            filter = filter.and(jsonWebKey -> Strings.CI.equals(jsonWebKey.getKeyId(), keyIdRequest.get()));
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
                                                              final Optional<OidcJsonWebKeyUsage> usage)
            throws Exception {
        LOGGER.debug("Loading JSON web key from [{}]", resource);
        try (val is = resource.getInputStream()) {
            val json = IOUtils.toString(is, StandardCharsets.UTF_8);
            LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
            return buildJsonWebKeySet(json, keyId, usage);
        }
    }

    private Optional<JsonWebKeySet> buildJsonWebKeySet(
        final String json, final Optional<String> keyId,
        final Optional<OidcJsonWebKeyUsage> usage)
            throws Exception {
        if (JsonUtils.isValidJsonObject(json)) {
            return getJsonWebKeyFromJsonWebKeySet(new JsonWebKeySet(json), keyId, usage);
        }
        val key = new AesKey(json.getBytes(StandardCharsets.UTF_8));
        val jsonWebKey = new OctetSequenceJsonWebKey(key);
        jsonWebKey.setKeyId(keyId.orElse(StringUtils.EMPTY));
        jsonWebKey.setUse(usage.map(Enum::name).orElse(StringUtils.EMPTY));
        return Optional.of(new JsonWebKeySet(jsonWebKey));
    }

    /**
     * Resolve the keystore of the application. Expressions are only evaluated in keystore locations,
     * which the deployment defines; keys defined inline as JSON are taken as they are, since they may
     * come from the application itself, through dynamic client registration or OpenID Federation.
     *
     * @param service the service
     * @return the keystore location, or the keys defined inline
     */
    public static String resolveJsonWebKeySet(final OidcRegisteredService service) {
        val jwks = StringUtils.trimToEmpty(service.getJwks());
        return jwks.startsWith("{") ? jwks : SpringExpressionLanguageValueResolver.getInstance().resolve(jwks);
    }

    /**
     * The keystore of the application: a location the deployment defines, keys published at a URL, or keys defined
     * inline. Keys published at an {@code http} or {@code https} URL are never requested at a link-local, unspecified or
     * multicast address, such as the metadata service of a cloud provider, and redirects are not followed, so that a
     * redirect cannot lead there either; addresses of private and loopback networks are allowed, since applications may
     * run there.
     *
     * @param service        the service
     * @param resourceLoader the resource loader
     * @return the resource, or null
     * @throws Exception when the keys cannot be located
     */
    private static @Nullable Resource getJsonWebKeySetResource(final OidcRegisteredService service,
                                                               final ResourceLoader resourceLoader)
            throws Exception {
        val serviceJwks = resolveJsonWebKeySet(service);
        if (StringUtils.isNotBlank(serviceJwks)) {
            if (Strings.CI.startsWithAny(serviceJwks, "http://", "https://")) {
                val url = new URI(serviceJwks).toURL();
                if (!DefaultOidcClientIdMetadataDocumentFetcher.isAllowedWithPrivateNetworks(url)) {
                    LOGGER.warn("Keys of [{}] at [{}] are not requested: the host resolves to an address that is not allowed",
                        service.getClientId(), serviceJwks);
                    return null;
                }
                return new UrlResource(url) {
                    @Override
                    protected void customizeConnection(final URLConnection connection) throws IOException {
                        super.customizeConnection(connection);
                        connection.setConnectTimeout(REMOTE_KEYS_TIMEOUT_MILLIS);
                        connection.setReadTimeout(REMOTE_KEYS_TIMEOUT_MILLIS);
                        if (connection instanceof final HttpURLConnection httpConnection) {
                            httpConnection.setInstanceFollowRedirects(false);
                        }
                    }
                };
            }
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
        if ("ec".equalsIgnoreCase(jwksType.trim())) {
            val spec = switch (jwksKeySize) {
                case JWK_EC_P384_SIZE -> EllipticCurves.P384;
                case JWK_EC_P512_SIZE, JWK_EC_P521_SIZE -> EllipticCurves.P521;
                default -> EllipticCurves.P256;
            };
            val jwk = generateJsonWebKeyEC(spec);
            jwk.setKeyId(UUID.randomUUID().toString());
            if (usage == OidcJsonWebKeyUsage.SIGNING) {
                jwk.setAlgorithm(getSigningAlgorithm(jwk));
            }
            usage.assignTo(jwk);
            return jwk;
        }
        val newJwk = FunctionUtils.doUnchecked(() -> RsaJwkGenerator.generateJwk(jwksKeySize));
        newJwk.setKeyId(UUID.randomUUID().toString());
        usage.assignTo(newJwk);
        return newJwk;
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
        val signingKeys = serviceJsonWebKeys
            .map(jwks -> jwks.getJsonWebKeys().stream().filter(OidcJsonWebKeyStoreUtils::isUsableForSigning).toList())
            .orElseGet(List::of);
        if (!signingKeys.isEmpty()) {
            LOGGER.debug("Found JSON web keys to sign the token: [{}]", signingKeys);
            return Optional.of(new JsonWebKeySet(signingKeys));
        }
        serviceJsonWebKeys.ifPresent(jwks -> LOGGER.debug("Service keys [{}] are reserved for encryption and are not used for signing", jwks));
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
     * Whether the JWS algorithm can be used with the key, i.e. the algorithm is known and expects the key's type,
     * and for an elliptic-curve key, is the algorithm of its curve.
     *
     * @param key       the key
     * @param algorithm the algorithm
     * @return true/false
     */
    public static boolean isSigningAlgorithmCompatible(final JsonWebKey key, final @Nullable String algorithm) {
        if (StringUtils.isBlank(algorithm)) {
            return false;
        }
        try {
            val jwsAlgorithm = AlgorithmFactoryFactory.getInstance().getJwsAlgorithmFactory().getAlgorithm(algorithm);
            return Strings.CI.equals(jwsAlgorithm.getKeyType(), key.getKeyType())
                && (!(key instanceof EllipticCurveJsonWebKey) || Strings.CS.equals(algorithm, getSigningAlgorithm(key)));
        } catch (final InvalidAlgorithmException e) {
            LOGGER.debug("Unknown signing algorithm [{}]: [{}]", algorithm, e.getMessage());
            return false;
        }
    }

    /**
     * Signing algorithm that matches the key: the ECDSA algorithm of an elliptic-curve key's curve,
     * or {@code RS256} for an RSA key.
     *
     * @param key the key
     * @return the algorithm, or null for other key types and unknown curves
     */
    public static @Nullable String getSigningAlgorithm(final JsonWebKey key) {
        return switch (key) {
            case final EllipticCurveJsonWebKey ecKey -> switch (StringUtils.defaultString(ecKey.getCurveName())) {
                case EllipticCurves.P_256 -> AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;
                case EllipticCurves.P_384 -> AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384;
                case EllipticCurves.P_521 -> AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512;
                case EllipticCurves.SECP_256K1 -> AlgorithmIdentifiers.ECDSA_USING_SECP256K1_CURVE_AND_SHA256;
                default -> null;
            };
            case RsaJsonWebKey _ -> AlgorithmIdentifiers.RSA_USING_SHA256;
            default -> null;
        };
    }

    /**
     * Correct elliptic-curve keys tagged with an ECDSA algorithm that does not match their curve, as keys
     * generated by earlier versions were. Signing keys get the algorithm of their curve and encryption keys
     * lose the signing algorithm. Other keys are left as they are.
     *
     * @param jsonWebKeySet the json web key set
     * @return the same json web key set
     */
    public static JsonWebKeySet normalizeSigningAlgorithms(final JsonWebKeySet jsonWebKeySet) {
        jsonWebKeySet.getJsonWebKeys()
            .stream()
            .filter(EllipticCurveJsonWebKey.class::isInstance)
            .filter(key -> Strings.CS.startsWith(key.getAlgorithm(), "ES"))
            .forEach(key -> {
                val encryption = OidcJsonWebKeyUsage.ENCRYPTION.is(key);
                val algorithm = encryption ? null : getSigningAlgorithm(key);
                if ((encryption || algorithm != null) && !Strings.CS.equals(algorithm, key.getAlgorithm())) {
                    LOGGER.debug("JSON web key [{}] on curve [{}] is tagged with [{}] and will use [{}] instead",
                        key.getKeyId(), ((EllipticCurveJsonWebKey) key).getCurveName(), key.getAlgorithm(), algorithm);
                    key.setAlgorithm(algorithm);
                }
            });
        return jsonWebKeySet;
    }

    /**
     * Whether the key may be used for signing operations: it carries key material and is not reserved for encryption.
     * Public keys stay usable, since the same keys verify objects the application signs, such as request objects.
     *
     * @param key the key
     * @return true/false
     */
    private static boolean isUsableForSigning(final JsonWebKey key) {
        return key.getKey() != null && !OidcJsonWebKeyUsage.ENCRYPTION.is(key);
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
