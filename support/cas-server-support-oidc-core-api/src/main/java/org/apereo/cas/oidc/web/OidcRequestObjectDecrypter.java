package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratedEvent;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotatedEvent;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwx.HeaderParameterNames;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;

/**
 * Decrypts request objects that clients encrypt to CAS (OpenID Connect Core 1.0, section 6.3.1; RFC 9101, section 4).
 * A request object is encrypted to one of the encryption keys of the OpenID Connect keystore, which CAS publishes in its
 * JWKS, and names it with {@code kid}; without a {@code kid}, each encryption key is tried in turn. Every encryption key
 * the JWKS publishes is accepted, whatever its rotation state, since clients may still hold a key set from before a
 * rotation. The keys are read from the keystore once per JWKS cache period, not for every request, so that encrypted
 * request objects, which anyone can send, do not make CAS read and parse its keystore each time. Only the key management
 * and content encryption algorithms that discovery advertises are accepted, except {@code RSA1_5}, which is never
 * accepted (RFC 8725, section 3.2), and compressed request objects are refused.
 * <p>
 * The result is the nested JWT, normally signed by the client, which is then verified with the client's keys like any
 * signed request object. A request object encrypted without being signed comes back as an unsigned JWT, and is refused
 * as any unsigned request object is. Symmetric key wrapping, with keys derived from client secrets, is not supported.
 * <p>
 * The keystore generator service is looked up when the keys are first read, since it publishes the keystore events this
 * decrypter listens to while it is created.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcRequestObjectDecrypter {
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "oidcRequestObjectDecrypter";

    private static final int ENCRYPTED_JWT_PARTS_SEPARATORS = 4;

    private static final String DECRYPTION_KEYS = "decryptionKeys";

    private static final Duration MISSING_DECRYPTION_KEYS_EXPIRATION = Duration.ofMinutes(1);

    private final Supplier<OidcJsonWebKeystoreGeneratorService> keystoreGeneratorService;

    private final CasConfigurationProperties casProperties;

    private final Cache<String, List<PublicJsonWebKey>> decryptionKeys;

    public OidcRequestObjectDecrypter(final Supplier<OidcJsonWebKeystoreGeneratorService> keystoreGeneratorService,
                                      final CasConfigurationProperties casProperties) {
        this.keystoreGeneratorService = keystoreGeneratorService;
        this.casProperties = casProperties;
        val expiration = Beans.newDuration(casProperties.getAuthn().getOidc().getJwks().getCore().getJwksCacheExpiration());
        val missingExpiration = expiration.compareTo(MISSING_DECRYPTION_KEYS_EXPIRATION) < 0 ? expiration : MISSING_DECRYPTION_KEYS_EXPIRATION;
        this.decryptionKeys = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfter(Expiry.<String, List<PublicJsonWebKey>>creating((key, keys) -> keys.isEmpty() ? missingExpiration : expiration))
            .build();
    }

    /**
     * Forget the decryption keys once the keystore file changes or is generated.
     *
     * @param event the event
     */
    @EventListener
    public void handleOidcJsonWebKeystoreModifiedEvent(final OidcJsonWebKeystoreModifiedEvent event) {
        LOGGER.debug("Detected change in [{}]; request object decryption keys will be read again", event.getFile());
        decryptionKeys.invalidateAll();
    }

    /**
     * Forget the decryption keys once the keystore is generated.
     *
     * @param event the event
     */
    @EventListener
    public void handleOidcJsonWebKeystoreGeneratedEvent(final OidcJsonWebKeystoreGeneratedEvent event) {
        LOGGER.debug("Keystore [{}] is generated; request object decryption keys will be read again", event.getFile());
        decryptionKeys.invalidateAll();
    }

    /**
     * Forget the decryption keys once keys are rotated or revoked, so that a revoked encryption key stops decrypting at
     * once rather than when the keys would be read again.
     *
     * @param event the event
     */
    @EventListener
    public void handleOidcJsonWebKeystoreRotatedEvent(final OidcJsonWebKeystoreRotatedEvent event) {
        LOGGER.debug("Keys in the keystore are rotated or revoked; request object decryption keys will be read again");
        decryptionKeys.invalidateAll();
    }

    /**
     * Whether the request object is encrypted: a JWE in compact serialization has five parts.
     *
     * @param requestObject the request object
     * @return true/false
     */
    public static boolean isEncrypted(final String requestObject) {
        return StringUtils.countMatches(requestObject, '.') == ENCRYPTED_JWT_PARTS_SEPARATORS;
    }

    /**
     * Decrypt the request object.
     *
     * @param requestObject the encrypted request object
     * @return the nested JWT, in compact serialization
     * @throws IllegalArgumentException when the request object cannot be decrypted with any acceptable key
     */
    public String decrypt(final String requestObject) {
        val keyId = readKeyId(requestObject);
        val keys = getDecryptionKeys()
            .stream()
            .filter(key -> StringUtils.isBlank(keyId) || Strings.CS.equals(keyId, key.getKeyId()))
            .toList();
        for (val key : keys) {
            try {
                return toNestedJwt(decryptWith(requestObject, key));
            } catch (final Exception e) {
                LOGGER.debug("Request object cannot be decrypted with key [{}]: [{}]", key.getKeyId(), e.getMessage());
            }
        }
        throw new IllegalArgumentException("Request object cannot be decrypted with any encryption key of CAS");
    }

    /**
     * Encryption keys of the OpenID Connect keystore that have a private key, in any rotation state, never signing keys,
     * which the keystore lookup falls back to when it holds no encryption key, as read once per
     * JWKS cache period, or until the keystore changes, is generated, or has keys rotated or revoked. A keystore that
     * cannot be read, or has no such key, is read again after at most a minute, not on every call, so that encrypted
     * request objects sent to a CAS without encryption keys do not make it read its keystore each time.
     *
     * @return the keys
     */
    public List<PublicJsonWebKey> getDecryptionKeys() {
        return Objects.requireNonNullElseGet(decryptionKeys.get(DECRYPTION_KEYS, _ -> loadDecryptionKeys()), List::of);
    }

    private List<PublicJsonWebKey> loadDecryptionKeys() {
        val keys = FunctionUtils.doAndHandle(() -> {
            val keystore = OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(keystoreGeneratorService.get().generate());
            return OidcJsonWebKeyStoreUtils.getJsonWebKeyFromJsonWebKeySet(keystore, Optional.empty(), Optional.of(OidcJsonWebKeyUsage.ENCRYPTION))
                .map(JsonWebKeySet::getJsonWebKeys)
                .orElseGet(List::of)
                .stream()
                .filter(PublicJsonWebKey.class::isInstance)
                .map(PublicJsonWebKey.class::cast)
                .filter(key -> !OidcJsonWebKeyUsage.SIGNING.is(key))
                .filter(key -> key.getPrivateKey() != null)
                .toList();
        }, e -> {
            LOGGER.warn("Unable to read the encryption keys of the OpenID Connect keystore: [{}]", e.getMessage());
            return List.<PublicJsonWebKey>of();
        }).get();
        return Objects.requireNonNullElseGet(keys, List::of);
    }

    private String decryptWith(final String requestObject, final PublicJsonWebKey key) throws Exception {
        val discovery = casProperties.getAuthn().getOidc().getDiscovery();
        val jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(requestObject);
        if (jwe.getHeader(HeaderParameterNames.ZIP) != null) {
            throw new IllegalArgumentException("Compressed request objects are not supported");
        }
        jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            discovery.getRequestObjectEncryptionAlgValuesSupported()
                .stream()
                .filter(algorithm -> !KeyManagementAlgorithmIdentifiers.RSA1_5.equals(algorithm))
                .toArray(String[]::new)));
        jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            discovery.getRequestObjectEncryptionEncodingValuesSupported().toArray(String[]::new)));
        jwe.setKey(key.getPrivateKey());
        return jwe.getPayload();
    }

    private static @Nullable String readKeyId(final String requestObject) {
        try {
            val jwe = new JsonWebEncryption();
            jwe.setCompactSerialization(requestObject);
            return jwe.getKeyIdHeaderValue();
        } catch (final Exception e) {
            throw new IllegalArgumentException("Request object is not a valid encrypted JWT", e);
        }
    }

    /**
     * The payload of an encrypted request object is the nested JWT, or, for a request object that was encrypted without
     * being signed, the claims themselves, which are handed on as an unsigned JWT and so are refused, since anyone can
     * encrypt to the keys CAS publishes.
     *
     * @param payload the decrypted payload
     * @return the nested JWT
     * @throws Exception the exception
     */
    private static String toNestedJwt(final String payload) throws Exception {
        val trimmed = StringUtils.trim(payload);
        return Strings.CS.startsWith(trimmed, "{")
            ? new PlainJWT(JWTClaimsSet.parse(trimmed)).serialize()
            : trimmed;
    }
}
