package org.apereo.cas.util.crypto;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DecodableCipher}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface DecodableCipher<I, O> {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(DecodableCipher.class);

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

}
