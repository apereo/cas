package org.apereo.cas.util.cipher;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.keys.RsaKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

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
public abstract class BaseStringCipherExecutor extends AbstractCipherExecutor<Serializable, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStringCipherExecutor.class);

    private static final int ENCRYPTION_KEY_SIZE = 256;

    private static final int SIGNING_KEY_SIZE = 512;

    private String encryptionAlgorithm = KeyManagementAlgorithmIdentifiers.DIRECT;
    private String contentEncryptionAlgorithmIdentifier;

    private Key secretKeyEncryptionKey;

    private boolean encryptionEnabled = true;

    private BaseStringCipherExecutor() {
    }

    /**
     * Instantiates a new cipher.
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     *
     * @param secretKeyEncryption the secret key encryption; must be represented as a octet sequence JSON Web Key (JWK)
     * @param secretKeySigning    the secret key signing; must be represented as a octet sequence JSON Web Key (JWK)
     * @param encryptionEnabled   the enable encryption
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning,
                                    final boolean encryptionEnabled) {
        this(secretKeyEncryption, secretKeySigning, EncodingUtils.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, encryptionEnabled);
    }

    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning,
                                    final String alg) {
        this(secretKeyEncryption, secretKeySigning, alg, true);
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
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning) {
        this(secretKeyEncryption, secretKeySigning, EncodingUtils.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, true);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param secretKeyEncryption                  the key for encryption
     * @param secretKeySigning                     the key for signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     * @param encryptionEnabled                    the encryption enabled
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning,
                                    final String contentEncryptionAlgorithmIdentifier,
                                    final boolean encryptionEnabled) {
        super();
        this.encryptionEnabled = encryptionEnabled;
        if (this.encryptionEnabled) {
            configureEncryptionParameters(secretKeyEncryption, contentEncryptionAlgorithmIdentifier);
        } else {
            LOGGER.warn("Encryption is not enabled for [{}]. The cipher [{}] will only produce signed objects", getName(), getClass().getSimpleName());
        }
        configureSigningParameters(secretKeySigning);
    }

    private void configureSigningParameters(final String secretKeySigning) {
        String signingKeyToUse = secretKeySigning;
        if (StringUtils.isBlank(signingKeyToUse)) {
            LOGGER.warn("Secret key for signing is not defined for [{}]. CAS will attempt to auto-generate the signing key", getName());
            signingKeyToUse = EncodingUtils.generateJsonWebKey(SIGNING_KEY_SIZE);
            LOGGER.warn("Generated signing key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                    signingKeyToUse, SIGNING_KEY_SIZE, getName(), getSigningKeySetting());
        } else {
            LOGGER.debug("Located signing key to use for [{}]", getName());
        }
        setSigningKey(signingKeyToUse);
    }

    private void configureEncryptionParameters(final String secretKeyEncryption, final String contentEncryptionAlgorithmIdentifier) {
        String secretKeyToUse = secretKeyEncryption;
        if (StringUtils.isBlank(secretKeyToUse)) {
            LOGGER.warn("Secret key for encryption is not defined for [{}]; CAS will attempt to auto-generate the encryption key", getName());
            secretKeyToUse = EncodingUtils.generateJsonWebKey(ENCRYPTION_KEY_SIZE);
            LOGGER.warn("Generated encryption key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings under setting [{}].",
                    secretKeyToUse, ENCRYPTION_KEY_SIZE, getName(), getEncryptionKeySetting());
        } else {
            LOGGER.debug("Located encryption key to use for [{}]", getName());
        }

        try {
            if (ResourceUtils.isFile(secretKeyToUse) && ResourceUtils.doesResourceExist(secretKeyToUse)) {
                final Resource resource = ResourceUtils.getResourceFrom(secretKeyToUse);
                LOGGER.debug("Located encryption key resource [{}]. Attempting to extract public key...", resource);

                final PublicKeyFactoryBean factory = new PublicKeyFactoryBean();
                factory.setAlgorithm(RsaKeyUtil.RSA);
                factory.setLocation(resource);
                factory.setSingleton(false);
                this.secretKeyEncryptionKey = factory.getObject();
                this.encryptionAlgorithm = KeyManagementAlgorithmIdentifiers.RSA_OAEP_256;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (this.secretKeyEncryptionKey == null) {
                LOGGER.debug("Creating encryption key instance based on provided secret key");
                this.secretKeyEncryptionKey = EncodingUtils.generateJsonWebKey(secretKeyToUse);
            }
            this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;
            LOGGER.debug("Initialized cipher encryption sequence via content encryption [{}] and algorithm [{}]",
                    this.contentEncryptionAlgorithmIdentifier, this.encryptionAlgorithm);
        }
    }

    @Override
    public String encode(final Serializable value) {
        final String encoded = this.encryptionEnabled
                ? EncodingUtils.encryptValueAsJwt(this.secretKeyEncryptionKey, value, this.encryptionAlgorithm, this.contentEncryptionAlgorithmIdentifier)
                : value.toString();
        return new String(sign(encoded.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    @Override
    public String decode(final Serializable value) {
        try {
            final byte[] encoded = verifySignature(value.toString().getBytes(StandardCharsets.UTF_8));
            if (encoded != null && encoded.length > 0) {
                final String encodedObj = new String(encoded, StandardCharsets.UTF_8);
                return this.encryptionEnabled ? EncodingUtils.decryptJwtValue(this.secretKeyEncryptionKey, encodedObj) : encodedObj;
            }
            return null;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    /**
     * Gets encryption key setting.
     *
     * @return the encryption key setting
     */
    protected abstract String getEncryptionKeySetting();

    /**
     * Gets signing key setting.
     *
     * @return the signing key setting
     */
    protected abstract String getSigningKeySetting();
}
