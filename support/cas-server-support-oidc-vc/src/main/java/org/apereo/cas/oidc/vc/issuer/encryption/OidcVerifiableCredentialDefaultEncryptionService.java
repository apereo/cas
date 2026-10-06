package org.apereo.cas.oidc.vc.issuer.encryption;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialEncryptionProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadata;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.Use;
import org.jose4j.jwx.HeaderParameterNames;
import org.jspecify.annotations.Nullable;

/**
 * Default {@link OidcVerifiableCredentialEncryptionService}. Requests are encrypted to the current encryption keys of
 * the OpenID Connect keystore, each published with the JWE algorithm it is used with: {@code RSA-OAEP-256} for an RSA
 * key and {@code ECDH-ES} for an elliptic curve key. A request names its key with the JWE {@code kid}, as
 * OpenID4VCI 1.0 section 10 requires when the key has one. Compression is not supported in either direction.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcVerifiableCredentialDefaultEncryptionService implements OidcVerifiableCredentialEncryptionService {
    protected final OidcConfigurationContext configurationContext;

    protected final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> keystoreCache;

    @Override
    public boolean isEnabled() {
        return getProperties().isEnabled() && !getRequestEncryptionKeys().isEmpty();
    }

    @Override
    public boolean isRequestEncryptionRequired() {
        return isEnabled() && getProperties().isRequestEncryptionRequired();
    }

    @Override
    public boolean isResponseEncryptionRequired() {
        return isEnabled() && getProperties().isResponseEncryptionRequired();
    }

    @Override
    public OidcCredentialIssuerMetadata.@Nullable CredentialRequestEncryption buildRequestEncryptionMetadata() {
        if (!isEnabled()) {
            return null;
        }
        val keys = getRequestEncryptionKeys()
            .stream()
            .map(OidcVerifiableCredentialDefaultEncryptionService::toPublishedKey)
            .toList();
        return OidcCredentialIssuerMetadata.CredentialRequestEncryption.builder()
            .jwks(Map.of(JsonWebKeySet.JWK_SET_MEMBER_NAME, keys))
            .encValuesSupported(getProperties().getEncValuesSupported())
            .encryptionRequired(getProperties().isRequestEncryptionRequired())
            .build();
    }

    @Override
    public OidcCredentialIssuerMetadata.@Nullable CredentialResponseEncryption buildResponseEncryptionMetadata() {
        if (!isEnabled()) {
            return null;
        }
        return OidcCredentialIssuerMetadata.CredentialResponseEncryption.builder()
            .algValuesSupported(getProperties().getAlgValuesSupported())
            .encValuesSupported(getProperties().getEncValuesSupported())
            .encryptionRequired(getProperties().isResponseEncryptionRequired())
            .build();
    }

    @Override
    public String decryptRequest(final String request) {
        val jwe = readEncryptedRequest(request);
        val keyId = jwe.getKeyIdHeaderValue();
        val key = getRequestEncryptionKeys()
            .stream()
            .filter(candidate -> candidate.getKeyId().equals(keyId))
            .findFirst()
            .orElseThrow(() -> OidcVerifiableCredentialEncryptionException.invalidRequest(
                "Encrypted credential request names no known encryption key"));
        if (jwe.getHeader(HeaderParameterNames.ZIP) != null) {
            throw OidcVerifiableCredentialEncryptionException.invalidRequest("Compressed credential requests are not supported");
        }
        jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            Objects.requireNonNull(resolveKeyManagementAlgorithm(key))));
        jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            getProperties().getEncValuesSupported().toArray(String[]::new)));
        jwe.setKey(key.getPrivateKey());
        try {
            return jwe.getPayload();
        } catch (final Exception e) {
            LOGGER.debug("Unable to decrypt credential request: [{}]", e.getMessage());
            throw OidcVerifiableCredentialEncryptionException.invalidRequest("Encrypted credential request cannot be decrypted");
        }
    }

    @Override
    public void validateResponseEncryption(final OidcVerifiableCredentialRequest.CredentialResponseEncryption parameters) {
        val key = toResponseEncryptionKey(parameters);
        val algorithm = key.getAlgorithm();
        if (!getProperties().getAlgValuesSupported().contains(algorithm)) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters(
                "Credential response encryption algorithm %s is not supported".formatted(algorithm));
        }
        val encryptionMethod = parameters.getEnc();
        if (StringUtils.isBlank(encryptionMethod) || !getProperties().getEncValuesSupported().contains(encryptionMethod)) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters(
                "Credential response content encryption algorithm %s is not supported".formatted(encryptionMethod));
        }
        if (StringUtils.isNotBlank(parameters.getZip())) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Compressed credential responses are not supported");
        }
        try {
            val factory = AlgorithmFactoryFactory.getInstance();
            val contentEncryption = factory.getJweContentEncryptionAlgorithmFactory().getAlgorithm(encryptionMethod);
            factory.getJweKeyManagementAlgorithmFactory().getAlgorithm(algorithm).validateEncryptionKey(key.getKey(), contentEncryption);
        } catch (final Exception e) {
            LOGGER.debug("Credential response encryption key cannot be used: [{}]", e.getMessage());
            throw OidcVerifiableCredentialEncryptionException.invalidParameters(
                "Credential response encryption key cannot be used with %s".formatted(algorithm));
        }
    }

    @Override
    public String encryptResponse(final String response, final OidcVerifiableCredentialRequest.CredentialResponseEncryption parameters) {
        val key = toResponseEncryptionKey(parameters);
        try {
            val jwe = new JsonWebEncryption();
            jwe.setAlgorithmHeaderValue(key.getAlgorithm());
            jwe.setEncryptionMethodHeaderParameter(parameters.getEnc());
            if (StringUtils.isNotBlank(key.getKeyId())) {
                jwe.setKeyIdHeaderValue(key.getKeyId());
            }
            jwe.setKey(key.getKey());
            jwe.setPayload(response);
            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            LOGGER.warn("Unable to encrypt credential response: [{}]", e.getMessage());
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response cannot be encrypted");
        }
    }

    /**
     * Current encryption keys of the OpenID Connect keystore that requests can be encrypted to: keys with a key id,
     * a private key and a type a JWE algorithm is known for.
     *
     * @return the keys, with their private keys
     */
    protected List<PublicJsonWebKey> getRequestEncryptionKeys() {
        val issuer = configurationContext.getIssuerService().determineIssuer(Optional.empty());
        val keystore = keystoreCache.get(new OidcJsonWebKeyCacheKey(issuer, OidcJsonWebKeyUsage.ENCRYPTION));
        if (keystore == null) {
            return List.of();
        }
        return keystore.getJsonWebKeys()
            .stream()
            .filter(PublicJsonWebKey.class::isInstance)
            .map(PublicJsonWebKey.class::cast)
            .filter(key -> StringUtils.isNotBlank(key.getKeyId()) && key.getPrivateKey() != null
                && resolveKeyManagementAlgorithm(key) != null)
            .toList();
    }

    protected OidcVerifiableCredentialEncryptionProperties getProperties() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer().getEncryption();
    }

    protected static @Nullable String resolveKeyManagementAlgorithm(final JsonWebKey key) {
        return switch (key) {
            case RsaJsonWebKey _ -> KeyManagementAlgorithmIdentifiers.RSA_OAEP_256;
            case EllipticCurveJsonWebKey _ -> KeyManagementAlgorithmIdentifiers.ECDH_ES;
            default -> null;
        };
    }

    /**
     * The public part of a request encryption key as published in the issuer metadata, marked for encryption and
     * carrying the JWE algorithm the wallet must use with it. The keystore's own copy is left untouched.
     *
     * @param key the key
     * @return the published key parameters
     */
    protected static Map<String, Object> toPublishedKey(final PublicJsonWebKey key) {
        val parameters = new LinkedHashMap<>(key.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
        parameters.put(JsonWebKey.USE_PARAMETER, Use.ENCRYPTION);
        parameters.put(JsonWebKey.ALGORITHM_PARAMETER, resolveKeyManagementAlgorithm(key));
        return parameters;
    }

    /**
     * The public key a wallet supplied for response encryption. OpenID4VCI 1.0 section 10 requires its {@code alg},
     * which is the JWE algorithm the response is encrypted with.
     *
     * @param parameters the parameters
     * @return the key
     */
    protected static PublicJsonWebKey toResponseEncryptionKey(final OidcVerifiableCredentialRequest.CredentialResponseEncryption parameters) {
        val jwk = parameters.getJwk();
        if (jwk == null || jwk.isEmpty()) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response encryption carries no jwk");
        }
        val key = readJsonWebKey(jwk);
        if (key.getPrivateKey() != null) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response encryption jwk must be a public key");
        }
        if (StringUtils.isBlank(key.getAlgorithm())) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response encryption jwk carries no alg");
        }
        if (StringUtils.isNotBlank(key.getUse()) && !Use.ENCRYPTION.equals(key.getUse())) {
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response encryption jwk is not an encryption key");
        }
        return key;
    }

    private static JsonWebEncryption readEncryptedRequest(final String request) {
        try {
            val jwe = new JsonWebEncryption();
            jwe.setCompactSerialization(request);
            return jwe;
        } catch (final Exception e) {
            LOGGER.debug("Unable to read encrypted credential request: [{}]", e.getMessage());
            throw OidcVerifiableCredentialEncryptionException.invalidRequest("Encrypted credential request is not a JWE");
        }
    }

    private static PublicJsonWebKey readJsonWebKey(final Map<String, Object> jwk) {
        try {
            return PublicJsonWebKey.Factory.newPublicJwk(jwk);
        } catch (final Exception e) {
            LOGGER.debug("Unable to read credential response encryption key: [{}]", e.getMessage());
            throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response encryption jwk is invalid");
        }
    }
}
