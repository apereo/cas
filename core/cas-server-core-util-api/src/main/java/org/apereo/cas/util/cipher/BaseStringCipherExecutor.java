package org.apereo.cas.util.cipher;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.PropertyBoundCipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.jwt.JsonWebTokenEncryptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * The {@link BaseStringCipherExecutor} is the default
 * implementation of {@link CipherExecutor}. It provides
 * a facade API to encrypt, sign, and verify values.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
public abstract class BaseStringCipherExecutor extends AbstractCipherExecutor<Serializable, String>
    implements PropertyBoundCipherExecutor<Serializable, String> {
    private CipherOperationsStrategyType strategyType = CipherOperationsStrategyType.ENCRYPT_AND_SIGN;

    private String encryptionAlgorithm = KeyManagementAlgorithmIdentifiers.DIRECT;

    private Key encryptionKey;

    private boolean encryptionEnabled = true;

    private boolean signingEnabled = true;

    private int encryptionKeySize = EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE;

    private int signingKeySize = SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE;

    private String secretKeyEncryption;

    private String secretKeySigning;

    private String contentEncryptionAlgorithmIdentifier;

    private boolean initialized;

    protected BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                       final boolean encryptionEnabled, final boolean signingEnabled,
                                       final int signingKeySize, final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM,
            encryptionEnabled, signingEnabled, signingKeySize, encryptionKeySize);
    }

    protected BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                       final boolean encryptionEnabled,
                                       final int signingKeySize,
                                       final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, encryptionEnabled,
            true, signingKeySize, encryptionKeySize);
    }

    protected BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                       final String contentEncryptionAlgorithmIdentifier,
                                       final int signingKeySize, final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier,
            true, true, signingKeySize, encryptionKeySize);
    }

    protected BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                       final int signingKeySize, final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM,
            true, true, signingKeySize, encryptionKeySize);
    }


    protected BaseStringCipherExecutor(final String secretKeyEncryption,
                                       final String secretKeySigning,
                                       final String contentEncryptionAlgorithmIdentifier,
                                       final boolean encryptionEnabled,
                                       final boolean signingEnabled,
                                       final int signingKeyLength,
                                       final int encryptionKeyLength) {
        this.secretKeyEncryption = secretKeyEncryption;
        this.secretKeySigning = secretKeySigning;
        this.signingEnabled = signingEnabled || StringUtils.isNotBlank(secretKeySigning);
        this.encryptionEnabled = encryptionEnabled || StringUtils.isNotBlank(secretKeyEncryption);
        this.signingKeySize = signingKeyLength <= 0
            ? SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE : signingKeyLength;
        this.encryptionKeySize = encryptionKeyLength <= 0
            ? EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE : encryptionKeyLength;
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;
        initialize();
    }

    @Override
    public String encode(final Serializable value, final Object[] parameters) {
        if (strategyType == CipherOperationsStrategyType.ENCRYPT_AND_SIGN) {
            return encryptAndSign(value, getEncryptionKey(), getSigningKey());
        }
        return signAndEncrypt(value, getEncryptionKey(), getSigningKey());
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        return decode(value, parameters, getEncryptionKey(), getSigningKey());
    }

    protected String decode(final Serializable value, final Object[] parameters,
                            final Key encKey, final Key signingKey) {
        if (strategyType == CipherOperationsStrategyType.ENCRYPT_AND_SIGN) {
            return verifyAndDecrypt(value, encKey, signingKey);
        }
        return decryptAndVerify(value, encKey, signingKey);
    }

    protected void initialize() {
        if (!initialized) {
            try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
                val signingCertTask = Unchecked.runnable(() -> {
                    if (this.signingEnabled) {
                        configureSigningParameters(secretKeySigning);
                    } else {
                        LOGGER.info("Signing is not enabled for [{}]. The cipher [{}] will attempt to produce plain objects", getName(), getClass().getSimpleName());
                    }
                });
                val encryptionCertTask = Unchecked.runnable(() -> {
                    if (this.encryptionEnabled) {
                        configureEncryptionParameters(secretKeyEncryption, contentEncryptionAlgorithmIdentifier);
                    } else {
                        LOGGER.debug("Encryption is not enabled for [{}]. The cipher [{}] will only attempt to produce signed objects",
                            getName(), getClass().getSimpleName());
                    }
                });
                executor.execute(signingCertTask);
                executor.execute(encryptionCertTask);
            }
            this.initialized = true;
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() || isEncryptionPossible(this.encryptionKey);
    }

    protected void configureEncryptionKeyFromPublicKeyResource(final String secretKeyToUse) {
        val object = extractPublicKeyFromResource(secretKeyToUse);
        LOGGER.debug("Located encryption key resource [{}]", secretKeyToUse);
        setEncryptionKey(object);
        setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
    }

    protected boolean isEncryptionPossible(final Key key) {
        return this.encryptionEnabled && key != null;
    }


    protected String encryptValueAsJwt(final Key encryptionKey, final Serializable value) {
        val headers = new LinkedHashMap<>(getCommonHeaders());
        headers.putAll(getEncryptionOpHeaders());
        return JsonWebTokenEncryptor.builder()
            .key(encryptionKey)
            .algorithm(encryptionAlgorithm)
            .encryptionMethod(contentEncryptionAlgorithmIdentifier)
            .headers(headers)
            .build()
            .encrypt(value);
    }

    private void configureSigningParameters(final String secretKeySigning) {
        var signingKeyToUse = secretKeySigning;
        if (StringUtils.isBlank(signingKeyToUse)) {
            LOGGER.warn("Secret key for signing is not defined for [{}]. CAS will attempt to auto-generate the signing key", getName());
            signingKeyToUse = EncodingUtils.generateJsonWebKey(this.signingKeySize);
            val prop = String.format("%s=%s", getSigningKeySetting(), signingKeyToUse);
            //CHECKSTYLE:OFF
            LOGGER.warn("Generated signing key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings:\n\n\t{}\n\n",
                signingKeyToUse, this.signingKeySize, getName(), prop);
            //CHECKSTYLE:ON
        } else {
            try {
                val jwk = (PublicJsonWebKey) EncodingUtils.newJsonWebKey(signingKeyToUse);
                LOGGER.trace("Parsed signing key as a JSON web key for [{}] with kid [{}]", getName(), jwk.getKeyId());
                if (jwk.getPrivateKey() == null) {
                    val msg = "Provided signing key as a JSON web key does not carry a private key";
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                }
                setSigningKey(jwk.getPrivateKey());
            } catch (final Exception e) {
                LOGGER.trace("Unable to recognize signing key for [{}] as a JSON web key: [{}].", getSigningKeySetting(), e.getMessage());
                LOGGER.debug("Using pre-defined signing key to use for [{}]", getSigningKeySetting());
            }
        }
        configureSigningKey(signingKeyToUse);
    }

    private void configureEncryptionParameters(final String secretKeyEncryption, final String contentEncryptionAlgorithmIdentifier) {
        var secretKeyToUse = secretKeyEncryption;
        if (StringUtils.isBlank(secretKeyToUse)) {
            LOGGER.warn("Secret key for encryption is not defined for [{}]; CAS will attempt to auto-generate the encryption key", getName());
            secretKeyToUse = EncodingUtils.generateJsonWebKey(this.encryptionKeySize);
            val prop = String.format("%s=%s", getEncryptionKeySetting(), secretKeyToUse);
            //CHECKSTYLE:OFF
            LOGGER.warn("Generated encryption key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings:\n\n\t{}\n\n",
                secretKeyToUse, this.encryptionKeySize, getName(), prop);
            //CHECKSTYLE:ON
        } else {
            try {
                val results = JsonUtil.parseJson(secretKeyToUse);
                LOGGER.trace("Parsed encryption key as a JSON web key for [{}] as [{}]", getName(), results);
                setEncryptionKey(EncodingUtils.generateJsonWebKey(results));
            } catch (final Exception e) {
                LOGGER.trace("Unable to recognize encryption key [{}] as a JSON web key: [{}].", getEncryptionKeySetting(), e.getMessage());
                LOGGER.debug("Using pre-defined encryption key to use for [{}]", getEncryptionKeySetting());
            }
        }
        try {
            if (ResourceUtils.doesResourceExist(secretKeyToUse)) {
                configureEncryptionKeyFromPublicKeyResource(secretKeyToUse);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            if (this.encryptionKey == null) {
                LOGGER.trace("Creating encryption key instance based on provided secret key");
                setEncryptionKey(EncodingUtils.generateJsonWebKey(secretKeyToUse));
            }
            if (StringUtils.isBlank(contentEncryptionAlgorithmIdentifier)) {
                setContentEncryptionAlgorithmIdentifier(EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
            } else {
                setContentEncryptionAlgorithmIdentifier(contentEncryptionAlgorithmIdentifier);
            }
            LOGGER.trace("Initialized cipher encryption sequence via content encryption [{}] and algorithm [{}]",
                this.contentEncryptionAlgorithmIdentifier, this.encryptionAlgorithm);
        }
    }

    private String decryptAndVerify(final Serializable value, final Key encryptionKey, final Key signingKey) {
        Objects.requireNonNull(value, () -> """
            Value to verify/decrypt cannot be null. This is likely because keys used to sign and encrypt the value do not match.
            """.stripIndent().trim());
        var encodedObj = value.toString();
        if (isEncryptionPossible(encryptionKey)) {
            LOGGER.trace("Attempting to decrypt value based on encryption key defined by [{}]", getEncryptionKeySetting());
            encodedObj = EncodingUtils.decryptJwtValue(encryptionKey, encodedObj);
        }
        val currentValue = encodedObj.getBytes(StandardCharsets.UTF_8);
        val encoded = FunctionUtils.doIf(this.signingEnabled, () -> {
            LOGGER.trace("Attempting to verify signature based on signing key defined by [{}]", getSigningKeySetting());
            return verifySignature(currentValue, signingKey);
        }, () -> currentValue).get();
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private String verifyAndDecrypt(final Serializable value, final Key encryptionKey, final Key signingKey) {
        Objects.requireNonNull(value, () -> """
            Value to verify/decrypt cannot be null. This is likely because keys used to sign and encrypt the value do not match.
            """.stripIndent().trim());
        
        val currentValue = value.toString().getBytes(StandardCharsets.UTF_8);
        val encoded = FunctionUtils.doIf(this.signingEnabled, () -> {
            LOGGER.trace("Attempting to verify signature based on signing key defined by [{}]", getSigningKeySetting());
            return verifySignature(currentValue, signingKey);
        }, () -> currentValue).get();

        if (encoded != null && encoded.length > 0) {
            val encodedObj = new String(encoded, StandardCharsets.UTF_8);

            if (isEncryptionPossible(encryptionKey)) {
                LOGGER.trace("Attempting to decrypt value based on encryption key defined by [{}]", getEncryptionKeySetting());
                return EncodingUtils.decryptJwtValue(encryptionKey, encodedObj);
            }
            return encodedObj;
        }
        return null;
    }

    private String encryptAndSign(final Serializable value, final Key encryptionKey, final Key signingKey) {
        val encoded = FunctionUtils.doIf(isEncryptionPossible(encryptionKey),
            () -> {
                LOGGER.trace("Attempting to encrypt value based on encryption key defined by [{}]", getEncryptionKeySetting());
                return encryptValueAsJwt(encryptionKey, value);
            },
            value::toString).get();

        if (this.signingEnabled) {
            LOGGER.trace("Attempting to sign value based on signing key defined by [{}]", getSigningKeySetting());
            val signed = sign(encoded.getBytes(StandardCharsets.UTF_8), signingKey);
            return new String(signed, StandardCharsets.UTF_8);
        }
        return encoded;
    }

    private String signAndEncrypt(final Serializable value, final Key encryptionKey, final Key signingKey) {
        val encoded = FunctionUtils.doIf(this.signingEnabled,
            () -> {
                LOGGER.trace("Attempting to sign value based on signing key defined by [{}]", getSigningKeySetting());
                val signed = sign(value.toString().getBytes(StandardCharsets.UTF_8), signingKey);
                return new String(signed, StandardCharsets.UTF_8);
            },
            value::toString
        ).get();

        return FunctionUtils.doIf(isEncryptionPossible(encryptionKey),
            () -> {
                LOGGER.trace("Attempting to encrypt value based on encryption key defined by [{}]", getEncryptionKeySetting());
                return encryptValueAsJwt(encryptionKey, encoded);
            },
            () -> encoded).get();
    }

    /**
     * Define the order of cipher operations.
     */
    public enum CipherOperationsStrategyType {
        /**
         * Encrypt the value first, and then sign.
         */
        ENCRYPT_AND_SIGN,
        /**
         * Sign the value first, and then encrypt.
         */
        SIGN_AND_ENCRYPT
    }
}
