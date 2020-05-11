package org.apereo.cas.util.cipher;

import org.apereo.cas.util.io.FileWatcherService;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is {@link JsonWebKeySetStringCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Setter
public class JsonWebKeySetStringCipherExecutor extends BaseStringCipherExecutor implements AutoCloseable, DisposableBean {
    private static final Map<String, String> KEY_MANAGEMENT_ALGORITHM_IDENTIFIER_MAP = new HashMap<>();

    static {
        KEY_MANAGEMENT_ALGORITHM_IDENTIFIER_MAP.put(RsaJsonWebKey.KEY_TYPE, KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        KEY_MANAGEMENT_ALGORITHM_IDENTIFIER_MAP.put(EllipticCurveJsonWebKey.KEY_TYPE, KeyManagementAlgorithmIdentifiers.ECDH_ES_A256KW);
    }

    private final FileWatcherService keystorePatchWatcherService;
    private final Optional<String> keyIdToUse;
    private final Optional<HttpsJwks> httpsJkws;

    private JsonWebKeySet webKeySet;

    public JsonWebKeySetStringCipherExecutor(final File jwksKeystore) {
        this(jwksKeystore, Optional.empty());
    }

    public JsonWebKeySetStringCipherExecutor(final File jwksKeystore, final String httpsJwksEndpointUrl) {
        this(jwksKeystore, Optional.empty(), httpsJwksEndpointUrl);
    }

    public JsonWebKeySetStringCipherExecutor(final File jwksKeystore, final Optional<String> keyId) {
        this(jwksKeystore, keyId, null);
    }

    @SneakyThrows
    public JsonWebKeySetStringCipherExecutor(final File jwksKeystore, final Optional<String> keyId,
                                             final String httpsJwksEndpointUrl) {

        val json = FileUtils.readFileToString(jwksKeystore, StandardCharsets.UTF_8);
        keystorePatchWatcherService = new FileWatcherService(jwksKeystore, file -> {
            try {
                val reloadedJson = FileUtils.readFileToString(jwksKeystore, StandardCharsets.UTF_8);
                this.webKeySet = new JsonWebKeySet(reloadedJson);
            } catch (final Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        });

        this.webKeySet = new JsonWebKeySet(json);
        this.keyIdToUse = keyId;
        this.httpsJkws = StringUtils.isNotBlank(httpsJwksEndpointUrl) ? Optional.of(new HttpsJwks(httpsJwksEndpointUrl)) : Optional.empty();

        this.keystorePatchWatcherService.start(getClass().getSimpleName());
        LOGGER.debug("Started JWKS watcher thread");

    }

    /**
     * Close.
     */
    @Override
    public void close() {
        if (this.keystorePatchWatcherService != null) {
            this.keystorePatchWatcherService.close();
        }
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    public String encode(final Serializable value, final Object[] parameters) {
        configureSigningParametersForEncoding();
        configureEncryptionParametersForEncoding();
        return super.encode(value, parameters);
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        configureSigningParametersForDecoding();
        configureEncryptionParametersForDecoding();
        return super.decode(value, parameters);
    }

    private void configureSigningParametersForDecoding() {
        val result = findPublicJsonWebKeyByProvidedKeyId(webKeySet.getJsonWebKeys());
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Could not locate public JSON web key from keystore");
        }
        val key = result.get();
        if (key.getPublicKey() == null) {
            throw new IllegalArgumentException("Public key located from keystore for key id " + key.getKeyId() + " is undefined");
        }
        setSigningKey(key.getPublicKey());
    }

    private void configureEncryptionParametersForDecoding() {
        if (httpsJkws.isEmpty()) {
            LOGGER.debug("No JWKS endpoint is defined. Configuration of encryption parameters and keys are skipped");
        } else {
            try {
                val keys = this.httpsJkws.get().getJsonWebKeys();
                val encKeyResult = findPublicJsonWebKey(keys, jsonWebKey -> true);

                if (encKeyResult.isEmpty()) {
                    throw new IllegalArgumentException("Could not locate JSON web key from endpoint");
                }
                val encKey = encKeyResult.get();
                if (encKey.getPrivateKey() == null) {
                    throw new IllegalArgumentException("Private key located from endpoint for key id " + encKey.getKeyId() + " is undefined");
                }
                setSecretKeyEncryptionKey(encKey.getPrivateKey());
                setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                val keyManagementAlgorithmIdentifier = KEY_MANAGEMENT_ALGORITHM_IDENTIFIER_MAP.get(encKey.getKeyType());
                if (keyManagementAlgorithmIdentifier == null) {
                    throw new IllegalArgumentException("Unsupported public key format");
                }
                setEncryptionAlgorithm(keyManagementAlgorithmIdentifier);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void configureEncryptionParametersForEncoding() {
        if (httpsJkws.isEmpty()) {
            LOGGER.debug("No JWKS endpoint is defined. Configuration of encryption parameters and keys are skipped");
        } else {
            try {
                val keys = this.httpsJkws.get().getJsonWebKeys();
                val encKeyResult = findPublicJsonWebKey(keys, jsonWebKey -> true);

                if (encKeyResult.isEmpty()) {
                    throw new IllegalArgumentException("Could not locate RSA JSON web key from endpoint");
                }
                val encKey = encKeyResult.get();
                if (encKey.getPublicKey() == null) {
                    throw new IllegalArgumentException("Public key located from endpoint for key id " + encKey.getKeyId() + " is undefined");
                }
                setSecretKeyEncryptionKey(encKey.getPublicKey());
                setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                val keyManagementAlgorithmIdentifier = KEY_MANAGEMENT_ALGORITHM_IDENTIFIER_MAP.get(encKey.getKeyType());
                if (keyManagementAlgorithmIdentifier == null) {
                    throw new IllegalArgumentException("Unsupported public key format");
                }
                setEncryptionAlgorithm(keyManagementAlgorithmIdentifier);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void configureSigningParametersForEncoding() {
        val result = findPublicJsonWebKeyByProvidedKeyId(webKeySet.getJsonWebKeys());
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Could not locate public JSON web key from keystore");
        }
        val key = result.get();
        if (key.getPrivateKey() == null) {
            throw new IllegalArgumentException("Private key located from keystore for key id " + key.getKeyId() + " is undefined");
        }
        setSigningKey(key.getPrivateKey());
    }

    private Optional<PublicJsonWebKey> findPublicJsonWebKeyByProvidedKeyId(final List<JsonWebKey> keys) {
        val predicate = this.keyIdToUse
            .<Predicate<JsonWebKey>>map(s -> jsonWebKey -> jsonWebKey.getKeyId()
            .equalsIgnoreCase(s))
            .orElseGet(() -> jsonWebKey -> true);
        return findPublicJsonWebKey(keys, predicate);
    }

    private static Optional<PublicJsonWebKey> findPublicJsonWebKey(final List<JsonWebKey> keys, final Predicate<JsonWebKey> filter) {
        return keys
            .stream()
            .filter(key -> key instanceof PublicJsonWebKey && filter.test(key))
            .map(PublicJsonWebKey.class::cast)
            .findFirst();
    }
}
