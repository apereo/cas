package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;

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
public abstract class BaseStringCipherExecutor extends AbstractCipherExecutor<Serializable, String> {
    private static final int ENCRYPTION_KEY_SIZE = 256;
    private static final int SIGNING_KEY_SIZE = 512;

    private String encryptionAlgorithm = KeyManagementAlgorithmIdentifiers.DIRECT;

    private String contentEncryptionAlgorithmIdentifier;

    private Key secretKeyEncryptionKey;

    private boolean encryptionEnabled = true;

    private boolean signingEnabled = true;

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
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final boolean encryptionEnabled, final boolean signingEnabled) {
        this(secretKeyEncryption, secretKeySigning, CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, encryptionEnabled, signingEnabled);
    }

    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final boolean encryptionEnabled) {
        this(secretKeyEncryption, secretKeySigning, encryptionEnabled, true);
    }

    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final String alg) {
        this(secretKeyEncryption, secretKeySigning, alg, true, true);
    }

    /**
     * Instantiates a new cipher.
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     *
     * @param secretKeyEncryption the secret key encryption; must be represented as a octet sequence JSON Web Key (JWK)
     * @param secretKeySigning    the secret key signing; must be represented as a octet sequence JSON Web Key (JWK)
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning) {
        this(secretKeyEncryption, secretKeySigning, CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, true, true);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param secretKeyEncryption                  the key for encryption
     * @param secretKeySigning                     the key for signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     * @param encryptionEnabled                    the encryption enabled
     * @param signingEnabled                       the signing enabled
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                    final String contentEncryptionAlgorithmIdentifier,
                                    final boolean encryptionEnabled, final boolean signingEnabled) {
        this.encryptionEnabled = encryptionEnabled || StringUtils.isNotBlank(secretKeyEncryption);
        this.signingEnabled = signingEnabled || StringUtils.isNotBlank(secretKeySigning);

        if (this.encryptionEnabled) {
            configureEncryptionParameters(secretKeyEncryption, contentEncryptionAlgorithmIdentifier);
        } else {
            LOGGER.warn("Encryption is not enabled for [{}]. The cipher [{}] will only attempt to produce signed objects",
                getName(), getClass().getSimpleName());
        }
        if (this.signingEnabled) {
            configureSigningParameters(secretKeySigning);
        } else {
            LOGGER.warn("Signing is not enabled for [{}]. The cipher [{}] will attempt to produce plain objects",
                getName(), getClass().getSimpleName());
        }
    }

    private void configureSigningParameters(final String secretKeySigning) {
        var signingKeyToUse = secretKeySigning;
        if (StringUtils.isBlank(signingKeyToUse)) {
            LOGGER.warn("Secret key for signing is not defined for [{}]. CAS will attempt to auto-generate the signing key", getName());
            signingKeyToUse = EncodingUtils.generateJsonWebKey(SIGNING_KEY_SIZE);
            LOGGER.warn("Generated signing key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                signingKeyToUse, SIGNING_KEY_SIZE, getName(), getSigningKeySetting());
        } else {
            LOGGER.debug("Located signing key to use for [{}]", getName());
        }
        configureSigningKey(signingKeyToUse);
    }

    private void configureEncryptionParameters(final String secretKeyEncryption, final String contentEncryptionAlgorithmIdentifier) {
        var secretKeyToUse = secretKeyEncryption;
        if (StringUtils.isBlank(secretKeyToUse)) {
            LOGGER.warn("Secret key for encryption is not defined for [{}]; CAS will attempt to auto-generate the encryption key", getName());
            secretKeyToUse = EncodingUtils.generateJsonWebKey(ENCRYPTION_KEY_SIZE);
            LOGGER.warn("Generated encryption key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                secretKeyToUse, ENCRYPTION_KEY_SIZE, getName(), getEncryptionKeySetting());
        } else {
            LOGGER.debug("Located encryption key to use for [{}]", getName());
        }
        try {
            if (ResourceUtils.doesResourceExist(secretKeyToUse)) {
                configureEncryptionKeyFromPublicKeyResource(secretKeyToUse);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (this.secretKeyEncryptionKey == null) {
                LOGGER.debug("Creating encryption key instance based on provided secret key");
                setSecretKeyEncryptionKey(EncodingUtils.generateJsonWebKey(secretKeyToUse));
            }
            setContentEncryptionAlgorithmIdentifier(contentEncryptionAlgorithmIdentifier);
            LOGGER.debug("Initialized cipher encryption sequence via content encryption [{}] and algorithm [{}]", this.contentEncryptionAlgorithmIdentifier, this.encryptionAlgorithm);
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
        val encoded = this.encryptionEnabled && this.secretKeyEncryptionKey != null
            ? EncodingUtils.encryptValueAsJwt(this.secretKeyEncryptionKey, value, this.encryptionAlgorithm, this.contentEncryptionAlgorithmIdentifier)
            : value.toString();

        if (this.signingEnabled) {
            val signed = sign(encoded.getBytes(StandardCharsets.UTF_8));
            return new String(signed, StandardCharsets.UTF_8);
        }
        return encoded;
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        val currentValue = value.toString().getBytes(StandardCharsets.UTF_8);
        val encoded = this.signingEnabled ? verifySignature(currentValue) : currentValue;

        if (encoded != null && encoded.length > 0) {
            val encodedObj = new String(encoded, StandardCharsets.UTF_8);

            if (this.encryptionEnabled && this.secretKeyEncryptionKey != null) {
                return EncodingUtils.decryptJwtValue(this.secretKeyEncryptionKey, encodedObj);
            }
            return encodedObj;
        }
        return null;
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

}
