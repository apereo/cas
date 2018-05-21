package org.apereo.cas.util.cipher;

import com.google.common.base.Predicates;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is {@link JsonWebKeySetStringCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class JsonWebKeySetStringCipherExecutor extends BaseStringCipherExecutor {
    private final JsonWebKeySet webKeySet;
    private final Optional<String> keyIdToUse;
    private final Optional<HttpsJwks> httpsJkws;

    public JsonWebKeySetStringCipherExecutor(final Resource jwksKeystore) {
        this(jwksKeystore, Optional.empty());
    }

    public JsonWebKeySetStringCipherExecutor(final Resource jwksKeystore, final String httpsJwksEndpointUrl) {
        this(jwksKeystore, Optional.empty(), httpsJwksEndpointUrl);
    }

    public JsonWebKeySetStringCipherExecutor(final Resource jwksKeystore, final Optional<String> keyId) {
        this(jwksKeystore, keyId, null);
    }

    @SneakyThrows
    public JsonWebKeySetStringCipherExecutor(final Resource jwksKeystore, final Optional<String> keyId,
                                             final String httpsJwksEndpointUrl) {
        final String json = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        this.webKeySet = new JsonWebKeySet(json);
        this.keyIdToUse = keyId;
        this.httpsJkws = StringUtils.isNotBlank(httpsJwksEndpointUrl) ? Optional.of(new HttpsJwks(httpsJwksEndpointUrl)) : Optional.empty();
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
        final Optional<RsaJsonWebKey> result = findRsaJsonWebKeyByProvidedKeyId(webKeySet.getJsonWebKeys());
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Could not locate RSA JSON web key from keystore");
        }
        final RsaJsonWebKey key = result.get();
        if (key.getPublicKey() == null) {
            throw new IllegalArgumentException("Public key located from keystore for key id " + key.getKeyId() + " is undefined");
        }
        setSigningKey(key.getPublicKey());
    }

    private void configureEncryptionParametersForDecoding() {
        if (!httpsJkws.isPresent()) {
            LOGGER.debug("No JWKS endpoint is defined. Configuration of encryption parameters and keys are skipped");
        } else {
            try {
                final List<JsonWebKey> keys = this.httpsJkws.get().getJsonWebKeys();
                final Optional<RsaJsonWebKey> encKeyResult = findRsaJsonWebKey(keys, Predicates.alwaysTrue());

                if (!encKeyResult.isPresent()) {
                    throw new IllegalArgumentException("Could not locate RSA JSON web key from endpoint");
                }
                final RsaJsonWebKey encKey = encKeyResult.get();
                if (encKey.getPrivateKey() == null) {
                    throw new IllegalArgumentException("Private key located from endpoint for key id " + encKey.getKeyId() + " is undefined");
                }
                setSecretKeyEncryptionKey(encKey.getPrivateKey());
                setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void configureEncryptionParametersForEncoding() {
        if (!httpsJkws.isPresent()) {
            LOGGER.debug("No JWKS endpoint is defined. Configuration of encryption parameters and keys are skipped");
        } else {
            try {
                final List<JsonWebKey> keys = this.httpsJkws.get().getJsonWebKeys();
                final Optional<RsaJsonWebKey> encKeyResult = findRsaJsonWebKey(keys, Predicates.alwaysTrue());

                if (!encKeyResult.isPresent()) {
                    throw new IllegalArgumentException("Could not locate RSA JSON web key from endpoint");
                }
                final RsaJsonWebKey encKey = encKeyResult.get();
                if (encKey.getPublicKey() == null) {
                    throw new IllegalArgumentException("Public key located from endpoint for key id " + encKey.getKeyId() + " is undefined");
                }
                setSecretKeyEncryptionKey(encKey.getPublicKey());
                setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void configureSigningParametersForEncoding() {
        final Optional<RsaJsonWebKey> result = findRsaJsonWebKeyByProvidedKeyId(webKeySet.getJsonWebKeys());
        if (!result.isPresent()) {
            throw new IllegalArgumentException("Could not locate RSA JSON web key from keystore");
        }
        final RsaJsonWebKey key = result.get();
        if (key.getPrivateKey() == null) {
            throw new IllegalArgumentException("Private key located from keystore for key id " + key.getKeyId() + " is undefined");
        }
        setSigningKey(key.getPrivateKey());
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "N/A";
    }

    @Override
    protected String getSigningKeySetting() {
        return "N/A";
    }

    private Optional<RsaJsonWebKey> findRsaJsonWebKeyByProvidedKeyId(final List<JsonWebKey> keys) {
        final Predicate<JsonWebKey> predicate = this.keyIdToUse.isPresent()
            ? jsonWebKey -> jsonWebKey.getKeyId().equalsIgnoreCase(this.keyIdToUse.get())
            : Predicates.alwaysTrue();

        return findRsaJsonWebKey(keys, predicate);
    }

    private Optional<RsaJsonWebKey> findRsaJsonWebKey(final List<JsonWebKey> keys, final Predicate<JsonWebKey> filter) {
        return keys
            .stream()
            .filter(key -> key instanceof RsaJsonWebKey && filter.test(key))
            .map(RsaJsonWebKey.class::cast)
            .findFirst();
    }
}
