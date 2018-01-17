package org.apereo.cas;

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Default content encryption algorithm.
     */
    String DEFAULT_CONTENT_ENCRYPTION_ALGORITHM =
        ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;

    /**
     * Encrypt the value. Implementations may
     * choose to also sign the final value.
     *
     * @param value the value
     * @return the encrypted value or null
     */
    O encode(I value);

    /**
     * Decode the value. Signatures may also be verified.
     *
     * @param value encrypted value
     * @return the decoded value.
     */
    O decode(I value);

    /**
     * Decode map.
     *
     * @param properties the properties
     * @return the map
     */
    default Map<String, Object> decode(Map<String, Object> properties) {
        final Map<String, Object> decrypted = new HashMap<>();
        properties.forEach((key, value) -> {
            try {
                LOGGER.debug("Attempting to decode key [{}]", key);
                final Object result = decode((I) value);
                if (result != null) {
                    LOGGER.debug("Decrypted key [{}] successfully", key);
                    decrypted.put(key, result);
                }
            } catch (final ClassCastException e) {
                LOGGER.debug("Value of key {}, is not the correct type, not decrypting, but using value as-is.", key);
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
}
