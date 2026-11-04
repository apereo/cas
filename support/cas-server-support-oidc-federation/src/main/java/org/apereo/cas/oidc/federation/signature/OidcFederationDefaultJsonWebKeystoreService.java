package org.apereo.cas.oidc.federation.signature;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * The Federation Entity Keys of CAS, kept in a JSON web keystore file that is generated when missing, with a current
 * and a future key. Keys carry the lifecycle {@code state} of the OpenID Connect keystore; a key without one is current,
 * so a keystore that predates rotation keeps working. Entity statements are signed with the current key, and the entity
 * configuration publishes the current, future and previous keys, so that a key is known to the federation before it
 * signs and after it stopped signing (OpenID Federation 1.0, section 11.2). Keys that are retired are kept in the same
 * file, public only, with {@code exp} set to the time they were retired, and are published by the historical keys
 * endpoint (section 8.7). New keys record {@code iat}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class OidcFederationDefaultJsonWebKeystoreService implements OidcFederationJsonWebKeystoreService {
    private static final String ISSUED_AT = "iat";

    private static final String EXPIRATION = "exp";

    private static final String REVOKED = "revoked";

    private static final String REVOKED_AT = "revoked_at";

    private static final String REASON = "reason";

    private final CasReentrantLock lock = new CasReentrantLock();

    private final OidcProperties oidcProperties;

    private final @Nullable Resource resource;

    private volatile List<JsonWebKey> keys;

    public OidcFederationDefaultJsonWebKeystoreService(final OidcProperties oidcProperties) throws Exception {
        this.oidcProperties = oidcProperties;
        val jwksFile = SpringExpressionLanguageValueResolver.getInstance().resolve(oidcProperties.getFederation().getJwksFile());
        this.resource = ResourceUtils.getRawResourceFrom(jwksFile);
        this.keys = loadOrGenerate(jwksFile);
    }

    private List<JsonWebKey> loadOrGenerate(final String jwksFile) throws Exception {
        try {
            if (ResourceUtils.doesResourceExist(jwksFile)) {
                LOGGER.debug("Loading OpenID Connect Federation JSON web keys from [{}]", resource);
                val jwks = IOUtils.toString(Objects.requireNonNull(resource).getInputStream(), StandardCharsets.UTF_8);
                val loaded = new JsonWebKeySet(jwks).getJsonWebKeys();
                if (!loaded.isEmpty()) {
                    return new ArrayList<>(loaded);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.debug("JSON web keys for OpenID Connect Federation are not found. Generating a current and a future key");
        val generated = new ArrayList<JsonWebKey>();
        generated.add(generateKey(JsonWebKeyLifecycleStates.CURRENT));
        generated.add(generateKey(JsonWebKeyLifecycleStates.FUTURE));
        if (!ResourceUtils.doesResourceExist(jwksFile)) {
            store(generated);
        }
        return generated;
    }

    /**
     * The published keys carry the algorithm they sign with, which verifiers match against the {@code alg} of a
     * statement, whatever {@code alg} the keystore records for them.
     *
     * @return the key set
     * @throws Exception the exception
     */
    @Override
    public JWKSet toJWKSet() throws Exception {
        val published = new ArrayList<JWK>();
        for (val key : getPublishedKeys()) {
            val jwk = toJwk(key);
            val parameters = jwk.toPublicJWK().toJSONObject();
            parameters.put(JsonWebKey.ALGORITHM_PARAMETER, resolveSigningAlgorithm(jwk).getName());
            published.add(JWK.parse(parameters));
        }
        return new JWKSet(published);
    }

    @Override
    public EntityStatement signEntityStatement(final EntityStatementClaimsSet claims) throws Exception {
        val signingKey = toJwk(getCurrentKey());
        return EntityStatement.sign(claims, signingKey, resolveSigningAlgorithm(signingKey));
    }

    @Override
    public SignedJWT sign(final JWTClaimsSet claims, final JOSEObjectType type) throws Exception {
        val signingKey = toJwk(getCurrentKey());
        val algorithm = resolveSigningAlgorithm(signingKey);
        val jwt = new SignedJWT(new JWSHeader.Builder(algorithm).type(type).keyID(signingKey.getKeyID()).build(), claims);
        jwt.sign(new DefaultJWSSignerFactory().createJWSSigner(signingKey, algorithm));
        return jwt;
    }

    /**
     * The algorithm a key signs with, from its type and curve: the {@code alg} a key may carry is not trusted, since
     * keys generated for elliptic curves have carried one that does not match their curve.
     *
     * @param key the key
     * @return the algorithm
     */
    static JWSAlgorithm resolveSigningAlgorithm(final JWK key) {
        return switch (key) {
            case final ECKey ecKey when Curve.P_384.equals(ecKey.getCurve()) -> JWSAlgorithm.ES384;
            case final ECKey ecKey when Curve.P_521.equals(ecKey.getCurve()) -> JWSAlgorithm.ES512;
            case ECKey _ -> JWSAlgorithm.ES256;
            case OctetKeyPair _ -> JWSAlgorithm.EdDSA;
            default -> JWSAlgorithm.RS256;
        };
    }

    @Override
    public List<JWK> getHistoricalKeys() {
        return keys
            .stream()
            .filter(OidcFederationDefaultJsonWebKeystoreService::isRetired)
            .map(OidcFederationDefaultJsonWebKeystoreService::toPublicJwk)
            .toList();
    }

    @Override
    public JWKSet rotate() throws Exception {
        return lock.executeAndThrow(() -> {
            val updated = new ArrayList<>(keys);
            val futureKeys = getActiveKeys(updated, JsonWebKeyLifecycleStates.FUTURE);
            if (futureKeys.isEmpty()) {
                LOGGER.info("No future OpenID Connect Federation key is published yet; adding one, which the next rotation makes current");
            } else {
                getActiveKeys(updated, JsonWebKeyLifecycleStates.CURRENT)
                    .forEach(key -> JsonWebKeyLifecycleStates.setJsonWebKeyState(key, JsonWebKeyLifecycleStates.PREVIOUS));
                futureKeys.forEach(key -> JsonWebKeyLifecycleStates.setJsonWebKeyState(key, JsonWebKeyLifecycleStates.CURRENT));
            }
            updated.add(generateKey(JsonWebKeyLifecycleStates.FUTURE));
            store(updated);
            keys = updated;
            return toJWKSet();
        });
    }

    @Override
    public JWKSet revoke() throws Exception {
        return lock.executeAndThrow(() -> {
            val retiredAt = Instant.now(Clock.systemUTC()).getEpochSecond();
            val updated = new ArrayList<JsonWebKey>();
            for (val key : keys) {
                if (!isRetired(key) && JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isPrevious()) {
                    val retired = JsonWebKey.Factory.newJwk(key.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
                    retired.setOtherParameter(EXPIRATION, retiredAt);
                    updated.add(retired);
                    LOGGER.info("Retired OpenID Connect Federation key [{}]", key.getKeyId());
                } else {
                    updated.add(key);
                }
            }
            store(updated);
            keys = updated;
            return toJWKSet();
        });
    }

    @Override
    public Optional<JWKSet> revokeKey(final String keyId, @Nullable final String reason) throws Exception {
        return lock.executeAndThrow(() -> {
            if (keys.stream().noneMatch(key -> keyId.equals(key.getKeyId()))) {
                return Optional.<JWKSet>empty();
            }
            val revokedAt = Instant.now(Clock.systemUTC()).getEpochSecond();
            val revocation = new LinkedHashMap<String, Object>();
            revocation.put(REVOKED_AT, revokedAt);
            if (StringUtils.isNotBlank(reason)) {
                revocation.put(REASON, reason);
            }
            val updated = new ArrayList<JsonWebKey>();
            for (val key : keys) {
                if (keyId.equals(key.getKeyId())) {
                    val revoked = isRetired(key) ? key : JsonWebKey.Factory.newJwk(key.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
                    if (!isRetired(key)) {
                        revoked.setOtherParameter(EXPIRATION, revokedAt);
                    }
                    revoked.setOtherParameter(REVOKED, revocation);
                    updated.add(revoked);
                } else {
                    updated.add(key);
                }
            }
            if (getActiveKeys(updated, JsonWebKeyLifecycleStates.CURRENT).isEmpty()) {
                val futureKeys = getActiveKeys(updated, JsonWebKeyLifecycleStates.FUTURE);
                if (futureKeys.isEmpty()) {
                    updated.add(generateKey(JsonWebKeyLifecycleStates.CURRENT));
                } else {
                    JsonWebKeyLifecycleStates.setJsonWebKeyState(futureKeys.getFirst(), JsonWebKeyLifecycleStates.CURRENT);
                }
            }
            if (getActiveKeys(updated, JsonWebKeyLifecycleStates.FUTURE).isEmpty()) {
                updated.add(generateKey(JsonWebKeyLifecycleStates.FUTURE));
            }
            store(updated);
            keys = updated;
            LOGGER.warn("Revoked OpenID Connect Federation key [{}] for reason [{}]", keyId, StringUtils.defaultIfBlank(reason, "unspecified"));
            return Optional.of(toJWKSet());
        });
    }

    private List<JsonWebKey> getPublishedKeys() {
        return keys
            .stream()
            .filter(key -> !isRetired(key))
            .sorted(Comparator.comparingLong(key -> JsonWebKeyLifecycleStates.getJsonWebKeyState(key).getState()))
            .toList();
    }

    private JsonWebKey getCurrentKey() {
        return getActiveKeys(keys, JsonWebKeyLifecycleStates.CURRENT)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No current OpenID Connect Federation key is defined"));
    }

    private static List<JsonWebKey> getActiveKeys(final List<JsonWebKey> keys, final JsonWebKeyLifecycleStates state) {
        return keys
            .stream()
            .filter(key -> !isRetired(key) && JsonWebKeyLifecycleStates.getJsonWebKeyState(key).getState() == state.getState())
            .toList();
    }

    private static boolean isRetired(final JsonWebKey key) {
        return key.getOtherParameterValue(EXPIRATION, Long.class) != null;
    }

    private JsonWebKey generateKey(final JsonWebKeyLifecycleStates state) {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey(
            oidcProperties.getJwks().getCore().getJwksType(),
            oidcProperties.getJwks().getCore().getJwksKeySize(),
            OidcJsonWebKeyUsage.SIGNING);
        JsonWebKeyLifecycleStates.setJsonWebKeyState(key, state);
        key.setOtherParameter(ISSUED_AT, Instant.now(Clock.systemUTC()).getEpochSecond());
        key.setAlgorithm(FunctionUtils.doUnchecked(() -> resolveSigningAlgorithm(toJwk(key))).getName());
        LOGGER.debug("Generated OpenID Connect Federation key [{}] as [{}]", key.getKeyId(), state);
        return key;
    }

    private void store(final List<JsonWebKey> updated) throws Exception {
        if (resource == null) {
            throw new IllegalStateException("No keystore file is defined for OpenID Connect Federation keys");
        }
        val json = new JsonWebKeySet(updated).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        LOGGER.debug("Writing OpenID Connect Federation JSON web keys to [{}]", resource);
        FileUtils.write(resource.getFile(), json, StandardCharsets.UTF_8);
    }

    private static JWK toJwk(final JsonWebKey key) throws Exception {
        return JWK.parse(key.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
    }

    private static JWK toPublicJwk(final JsonWebKey key) {
        try {
            val publicKey = key instanceof final PublicJsonWebKey publicJsonWebKey
                ? JsonWebKey.Factory.newJwk(publicJsonWebKey.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY))
                : key;
            return JWK.parse(publicKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
