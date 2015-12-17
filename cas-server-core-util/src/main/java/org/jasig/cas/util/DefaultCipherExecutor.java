package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link DefaultCipherExecutor} is the default
 * implementation of {@link org.jasig.cas.CipherExecutor}. It provides
 * a facade API to encrypt, sign, and verify values.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("defaultCookieCipherExecutor")
public final class DefaultCipherExecutor extends AbstractCipherExecutor<String, String> {
    private String contentEncryptionAlgorithmIdentifier;

    private Key secretKeyEncryptionKey;

    /**
     * Instantiates a new cipher.
     *
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     * @param secretKeyEncryption the secret key encryption; must be represented as a octet sequence JSON Web Key (JWK)
     * @param secretKeySigning the secret key signing; must be represented as a octet sequence JSON Web Key (JWK)
     */
    @Autowired
    public DefaultCipherExecutor(@Value("${tgc.encryption.key:}")
                                 final String secretKeyEncryption,
                                 @Value("${tgc.signing.key:}")
                                 final String secretKeySigning) {
        this(secretKeyEncryption, secretKeySigning,
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param secretKeyEncryption the key for encryption
     * @param secretKeySigning the key for signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     */
    public DefaultCipherExecutor(final String secretKeyEncryption,
                                 final String secretKeySigning,
                                 final String contentEncryptionAlgorithmIdentifier) {

        super();

        if (StringUtils.isBlank(secretKeyEncryption)) {
            logger.debug("secretKeyEncryption is not defined");
            return;
        }
        if (StringUtils.isBlank(secretKeySigning)) {
            logger.debug("secretKeySigning is not defined");
            return;
        }
        if (StringUtils.isBlank(contentEncryptionAlgorithmIdentifier)) {
            logger.debug("contentEncryptionAlgorithmIdentifier is not defined");
            return;
        }

        setSigningKey(secretKeySigning);
        this.secretKeyEncryptionKey =  prepareJsonWebTokenKey(secretKeyEncryption);
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;

        logger.debug("Initialized cipher encryption sequence via [{}]",
                 contentEncryptionAlgorithmIdentifier);

    }

    @Override
    public String encode(final String value) {
        final String encoded = encryptValue(value.toString());
        final String signed = new String(sign(encoded.getBytes()));
        return signed;
    }

    @Override
    public String decode(final String value) {
        final byte[] encoded = verifySignature(value.getBytes());
        if (encoded != null && encoded.length > 0) {
            return decryptValue(new String(encoded));
        }
        return null;
    }

    /**
     * Prepare json web token key.
     *
     * @param secret the secret
     * @return the key
     */
    private Key prepareJsonWebTokenKey(final String secret) {
        try {
            final Map<String, Object> keys = new HashMap<>(2);
            keys.put("kty", "oct");
            keys.put("k", secret);
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
    private String encryptValue(@NotNull final String value) {
        try {
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(value);
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
            jwe.setEncryptionMethodHeaderParameter(this.contentEncryptionAlgorithmIdentifier);
            jwe.setKey(this.secretKeyEncryptionKey);
            logger.debug("Encrypting via [{}]", this.contentEncryptionAlgorithmIdentifier);
            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            throw new RuntimeException("Ensure that you have installed JCE Unlimited Strength Jurisdiction Policy Files. "
                    + e.getMessage(), e);
        }
    }

    /**
     * Decrypt value based on the key created during afterPropertiesSet.
     *
     * @param value the value
     * @return the decrypted value
     */
    private String decryptValue(@NotNull final String value) {
        try {
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setKey(this.secretKeyEncryptionKey);
            jwe.setCompactSerialization(value);
            logger.debug("Decrypting value...");
            return jwe.getPayload();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
