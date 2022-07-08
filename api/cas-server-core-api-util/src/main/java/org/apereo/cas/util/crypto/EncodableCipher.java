package org.apereo.cas.util.crypto;

import org.apache.commons.lang3.ArrayUtils;

/**
 * This is {@link EncodableCipher}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface EncodableCipher<I, O> {
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

}
