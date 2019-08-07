package org.apereo.cas.util.crypto;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible to define operation that deal with encryption, signing
 * and verification of a value.
 *
 * @author Misagh Moayyed
 * @param <I> the type parameter for the input
 * @param <O> the type parameter for the output
 * @since 4.1
 */
public interface CipherExecutor<I, O> {
    Logger LOGGER = LoggerFactory.getLogger(CipherExecutor.class);

    /**
     * The default content encryption algorithm.
     */
    String DEFAULT_CONTENT_ENCRYPTION_ALGORITHM =
        ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;

    /**
     * Encryption key size for text data and ciphers.
     */
    int DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE = 256;

    /**
     * Signing key size for text data and ciphers.
     */
    int DEFAULT_STRINGABLE_SIGNING_KEY_SIZE = 512;

    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor Serializable -> Serializable}
     */
    static CipherExecutor<Serializable, Serializable> noOp() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor String -> String}
     */
    static CipherExecutor<String, String> noOpOfStringToString() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor Serializable -> String}
     */
    static CipherExecutor<Serializable, String> noOpOfSerializableToString() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Encrypt the value. Implementations may
     * choose to also sign the final value.
     *
     * @param value      the value
     * @param parameters the parameters
     * @return the encrypted value or null
     */
    O encode(I value, Object[] parameters);

    /**
     * Encrypt the value.
     *
     * @param value the value
     * @return the encrypted value or null
     */
    default O encode(final I value) {
        return encode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Decode the value. Signatures may also be verified.
     *
     * @param value      encrypted value
     * @param parameters the parameters
     * @return the decoded value.
     */
    O decode(I value, Object[] parameters);

    /**
     * Decode the value.
     *
     * @param value the value
     * @return the decoded value or null
     */
    default O decode(final I value) {
        return decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Decode map.
     *
     * @param properties the properties
     * @param parameters the parameters
     * @return the map
     */
    default Map<String, Object> decode(final Map<String, Object> properties, final Object[] parameters) {
        val decrypted = new HashMap<String, Object>();
        properties.forEach((key, value) -> {
            try {
                LOGGER.trace("Attempting to decode key [{}]", key);
                val result = decode((I) value, parameters);
                if (result != null) {
                    LOGGER.trace("Decrypted key [{}] successfully", key);
                    decrypted.put(key, result);
                }
            } catch (final ClassCastException e) {
                LOGGER.debug("Value of key [{}], is not the correct type, not decrypting, but using value as-is.", key);
                decrypted.put(key, value);
            }
        });
        return decrypted;
    }

    /**
     * Supports encryption of values.
     *
     * @return true /false
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * The (component) name of this cipher.
     *
     * @return the name.
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Produce the signing key used to sign tokens in this cipher.
     *
     * @return key instance
     */
    default Key getSigningKey() {
        return null;
    }
}
