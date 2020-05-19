package org.apereo.cas.util.cipher;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * The {@link BaseStringCipherExecutor} is the default
 * implementation of {@link CipherExecutor}. It provides
 * a facade API to encrypt, sign, and verify values.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@NoArgsConstructor
@Setter
@Getter
public abstract class BaseStringCipherExecutor extends AbstractCipherExecutor<Serializable, String> {
    private CipherOperationsStrategyType strategyType = CipherOperationsStrategyType.ENCRYPT_AND_SIGN;

    private String encryptionAlgorithm = KeyManagementAlgorithmIdentifiers.DIRECT;

    private String contentEncryptionAlgorithmIdentifier;

    private Key secretKeyEncryptionKey;

    private boolean encryptionEnabled = true;

    private boolean signingEnabled = true;

    private int encryptionKeySize = CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE;

    private int signingKeySize = CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE;

    /**
     * Instantiates a new cipher.
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     *
     * @param secretKeyEncryption the secret key encryption; must be represented as a octet sequence JSON Web Key (JWK)
     * @param secretKeySigning    the secret key signing; must be represented as a octet sequence JSON Web Key (JWK)
     * @param encryptionEnabled   the enable encryption
     * @param signingEnabled      the signing enabled
     * @param signingKeySize      the signing key size
     * @param encryptionKeySize   the encryption key size
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                    final boolean encryptionEnabled, final boolean signingEnabled,
                                    final int signingKeySize, final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM,
            encryptionEnabled, signingEnabled, signingKeySize, encryptionKeySize);
    }

    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                    final boolean encryptionEnabled,
                                    final int signingKeySize,
                                    final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, encryptionEnabled, true, signingKeySize, encryptionKeySize);
    }

    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                    final String contentEncryptionAlgorithmIdentifier,
                                    final int signingKeySize,
                                    final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, contentEncryptionAlgorithmIdentifier, true, true, signingKeySize, encryptionKeySize);
    }

    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                    final int signingKeySize,
                                    final int encryptionKeySize) {
        this(secretKeyEncryption, secretKeySigning, CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM,
            true, true, signingKeySize, encryptionKeySize);
    }


    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning,
                                    final String contentEncryptionAlgorithmIdentifier,
                                    final boolean encryptionEnabled,
                                    final boolean signingEnabled,
                                    final int signingKeyLength,
                                    final int encryptionKeyLength) {

        this.signingEnabled = signingEnabled || StringUtils.isNotBlank(secretKeySigning);
        this.encryptionEnabled = this.signingEnabled && (encryptionEnabled || StringUtils.isNotBlank(secretKeyEncryption));
        this.signingKeySize = signingKeyLength <= 0 ? CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE : signingKeyLength;
        this.encryptionKeySize = encryptionKeyLength <= 0 ? CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE : encryptionKeyLength;

        if (this.encryptionEnabled) {
            configureEncryptionParameters(secretKeyEncryption, contentEncryptionAlgorithmIdentifier);
        } else {
            LOGGER.info("Encryption is not enabled for [{}]. The cipher [{}] will only attempt to produce signed objects",
                getName(), getClass().getSimpleName());
        }
        if (this.signingEnabled) {
            configureSigningParameters(secretKeySigning);
        } else {
            LOGGER.info("Signing is not enabled for [{}]. The cipher [{}] will attempt to produce plain objects", getName(), getClass().getSimpleName());
        }
    }

    private void configureSigningParameters(final String secretKeySigning) {
        var signingKeyToUse = secretKeySigning;
        if (StringUtils.isBlank(signingKeyToUse)) {
            LOGGER.warn("Secret key for signing is not defined for [{}]. CAS will attempt to auto-generate the signing key", getName());
            signingKeyToUse = EncodingUtils.generateJsonWebKey(this.signingKeySize);
            LOGGER.warn("Generated signing key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                signingKeyToUse, this.signingKeySize, getName(), getSigningKeySetting());
        } else {
            try {
                val jwk = PublicJsonWebKey.class.cast(EncodingUtils.newJsonWebKey(signingKeyToUse));
                LOGGER.trace("Parsed signing key as a JSON web key for [{}] with kid [{}]", getName(), jwk.getKeyId());
                if (jwk.getPrivateKey() == null) {
                    val msg = "Provided signing key as a JSON web key does not carry a private key";
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                }
                setSigningKey(jwk.getPrivateKey());
            } catch (final Exception e) {
                LOGGER.trace("[{}] Unable to recognize signing key as a JSON web key: [{}].", getName(), e.getMessage());
                LOGGER.debug("Using pre-defined signing key to use for [{}]", getName());
            }
        }
        configureSigningKey(signingKeyToUse);
    }

    private void configureEncryptionParameters(final String secretKeyEncryption, final String contentEncryptionAlgorithmIdentifier) {
        var secretKeyToUse = secretKeyEncryption;
        if (StringUtils.isBlank(secretKeyToUse)) {
            LOGGER.warn("Secret key for encryption is not defined for [{}]; CAS will attempt to auto-generate the encryption key", getName());
            secretKeyToUse = EncodingUtils.generateJsonWebKey(this.encryptionKeySize);
            LOGGER.warn("Generated encryption key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                secretKeyToUse, this.encryptionKeySize, getName(), getEncryptionKeySetting());
        } else {
            try {
                val results = JsonUtil.parseJson(secretKeyToUse);
                LOGGER.trace("Parsed encryption key as a JSON web key for [{}] as [{}]", getName(), results);
                setSecretKeyEncryptionKey(EncodingUtils.generateJsonWebKey(results));
            } catch (final Exception e) {
                LOGGER.trace("[{}] Unable to recognize encryption key as a JSON web key: [{}].", getName(), e.getMessage());
                LOGGER.debug("Using pre-defined encryption key to use for [{}]", getName());
            }
        }
        try {
            if (ResourceUtils.doesResourceExist(secretKeyToUse)) {
                configureEncryptionKeyFromPublicKeyResource(secretKeyToUse);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            if (this.secretKeyEncryptionKey == null) {
                LOGGER.trace("Creating encryption key instance based on provided secret key");
                setSecretKeyEncryptionKey(EncodingUtils.generateJsonWebKey(secretKeyToUse));
            }
            if (StringUtils.isBlank(contentEncryptionAlgorithmIdentifier)) {
                setContentEncryptionAlgorithmIdentifier(CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
            } else {
                setContentEncryptionAlgorithmIdentifier(contentEncryptionAlgorithmIdentifier);
            }
            LOGGER.trace("Initialized cipher encryption sequence via content encryption [{}] and algorithm [{}]",
                this.contentEncryptionAlgorithmIdentifier, this.encryptionAlgorithm);
        }
    }

    /**
     * Configure encryption key from public key resource.
     *
     * @param secretKeyToUse the secret key to use
     */
    protected void configureEncryptionKeyFromPublicKeyResource(final String secretKeyToUse) {
        val object = extractPublicKeyFromResource(secretKeyToUse);
        LOGGER.debug("Located encryption key resource [{}]", secretKeyToUse);
        setSecretKeyEncryptionKey(object);
        setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
    }

    @Override
    public String encode(final Serializable value, final Object[] parameters) {
        if (strategyType == CipherOperationsStrategyType.ENCRYPT_AND_SIGN) {
            return encryptAndSign(value);
        }
        return signAndEncrypt(value);
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        if (strategyType == CipherOperationsStrategyType.ENCRYPT_AND_SIGN) {
            return verifyAndDecrypt(value);
        }
        return decryptAndVerify(value);
    }

    private String decryptAndVerify(final Serializable value) {
        var encodedObj = value.toString();
        if (isEncryptionPossible()) {
            encodedObj = EncodingUtils.decryptJwtValue(this.secretKeyEncryptionKey, encodedObj);
        }
        val currentValue = encodedObj.getBytes(StandardCharsets.UTF_8);
        val encoded = this.signingEnabled ? verifySignature(currentValue) : currentValue;
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private String verifyAndDecrypt(final Serializable value) {
        val currentValue = value.toString().getBytes(StandardCharsets.UTF_8);
        val encoded = this.signingEnabled ? verifySignature(currentValue) : currentValue;

        if (encoded != null && encoded.length > 0) {
            val encodedObj = new String(encoded, StandardCharsets.UTF_8);

            if (isEncryptionPossible()) {
                return EncodingUtils.decryptJwtValue(this.secretKeyEncryptionKey, encodedObj);
            }
            return encodedObj;
        }
        return null;
    }

    private String encryptAndSign(final Serializable value) {
        val encoded = isEncryptionPossible()
            ? EncodingUtils.encryptValueAsJwt(this.secretKeyEncryptionKey, value,
            this.encryptionAlgorithm, this.contentEncryptionAlgorithmIdentifier, getCustomHeaders())
            : value.toString();

        if (this.signingEnabled) {
            val signed = sign(encoded.getBytes(StandardCharsets.UTF_8));
            return new String(signed, StandardCharsets.UTF_8);
        }
        return encoded;
    }

    private String signAndEncrypt(final Serializable value) {
        var encoded = value.toString();
        if (this.signingEnabled) {
            val signed = sign(encoded.getBytes(StandardCharsets.UTF_8));
            encoded = new String(signed, StandardCharsets.UTF_8);
        }

        return isEncryptionPossible()
            ? EncodingUtils.encryptValueAsJwt(this.secretKeyEncryptionKey, encoded,
            this.encryptionAlgorithm, this.contentEncryptionAlgorithmIdentifier, getCustomHeaders())
            : encoded;
    }

    /**
     * Is encryption possible?
     *
     * @return true/false
     */
    protected boolean isEncryptionPossible() {
        return this.encryptionEnabled && this.secretKeyEncryptionKey != null;
    }

    /**
     * Gets encryption key setting.
     *
     * @return the encryption key setting
     */
    protected String getEncryptionKeySetting() {
        return "N/A";
    }

    /**
     * Gets signing key setting.
     *
     * @return the signing key setting
     */
    protected String getSigningKeySetting() {
        return "N/A";
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
