package org.apereo.cas.util.cipher;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

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

    private String contentEncryptionAlgorithmIdentifier;

    private Key secretKeyEncryptionKey;

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
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning) {
        this(secretKeyEncryption, secretKeySigning,
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param secretKeyEncryption                  the key for encryption
     * @param secretKeySigning                     the key for signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning,
                                    final String contentEncryptionAlgorithmIdentifier) {

        super();

        if (StringUtils.isBlank(contentEncryptionAlgorithmIdentifier)) {
            LOGGER.debug("contentEncryptionAlgorithmIdentifier is not defined");
            return;
        }

        String secretKeyToUse = secretKeyEncryption;
        if (StringUtils.isBlank(secretKeyToUse)) {
            LOGGER.warn("Secret key for encryption is not defined for [{}]; CAS will attempt to auto-generate the encryption key", getName());
            secretKeyToUse = EncodingUtils.generateJsonWebKey(ENCRYPTION_KEY_SIZE);
            LOGGER.warn("Generated encryption key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings.",
                    secretKeyToUse, ENCRYPTION_KEY_SIZE, getName());
        } else {
            LOGGER.debug("Located encryption key to use for [{}]", getName());
        }

        String signingKeyToUse = secretKeySigning;
        if (StringUtils.isBlank(signingKeyToUse)) {
            LOGGER.warn("Secret key for signing is not defined for [{}]. CAS will attempt to auto-generate the signing key", getName());
            signingKeyToUse = EncodingUtils.generateJsonWebKey(SIGNING_KEY_SIZE);
            LOGGER.warn("Generated signing key [{}] of size [{}] for [{}]. The generated key MUST be added to CAS settings.",
                    signingKeyToUse, SIGNING_KEY_SIZE, getName());
        } else {
            LOGGER.debug("Located signing key to use for [{}]", getName());
        }


        setSigningKey(signingKeyToUse);
        this.secretKeyEncryptionKey = prepareJsonWebTokenKey(secretKeyToUse);
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;
        LOGGER.debug("Initialized cipher encryption sequence via [{}]", contentEncryptionAlgorithmIdentifier);

    }

    @Override
    public String encode(final Serializable value) {
        final String encoded = encryptValue(value);
        final String signed = new String(sign(encoded.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        return signed;
    }

    @Override
    public String decode(final Serializable value) {
        try {
            final byte[] encoded = verifySignature(value.toString().getBytes(StandardCharsets.UTF_8));
            if (encoded != null && encoded.length > 0) {
                return decryptValue(new String(encoded, StandardCharsets.UTF_8));
            }
            return null;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Prepare json web token key.
     *
     * @param secret the secret
     * @return the key
     */
    private static Key prepareJsonWebTokenKey(final String secret) {
        try {
            final Map<String, Object> keys = new HashMap<>(2);
            keys.put("kty", "oct");
            keys.put(EncodingUtils.JSON_WEB_KEY, secret);
            final JsonWebKey jwk = JsonWebKey.Factory.newJwk(keys);
            return jwk.getKey();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Encrypt the value based on the seed array whose length was given during afterPropertiesSet,
     * and the key and content encryption ids.
     *
     * @param value the value
     * @return the encoded value
     */
    private String encryptValue(final Serializable value) {
        try {
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(serializeValue(value));
            jwe.enableDefaultCompression();
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
            jwe.setEncryptionMethodHeaderParameter(this.contentEncryptionAlgorithmIdentifier);
            jwe.setKey(this.secretKeyEncryptionKey);
            LOGGER.debug("Encrypting via [{}]", this.contentEncryptionAlgorithmIdentifier);
            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            throw new RuntimeException("Ensure that you have installed JCE Unlimited Strength Jurisdiction Policy Files. "
                    + e.getMessage(), e);
        }
    }

    /**
     * Serialize value as string.
     *
     * @param value the value
     * @return the string
     */
    protected String serializeValue(final Serializable value) {
        return value.toString();
    }

    /**
     * Decrypt value based on the key created during afterPropertiesSet.
     *
     * @param value the value
     * @return the decrypted value
     */
    private String decryptValue(final String value) {
        try {
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setKey(this.secretKeyEncryptionKey);
            jwe.setCompactSerialization(value);
            LOGGER.debug("Decrypting value...");
            return jwe.getPayload();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
