package org.apereo.cas.util.crypto;

import module java.base;

/**
 * This is {@link IdentifiableKey}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface IdentifiableKey extends Key {
    /**
     * Gets id associated with this key.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the real key.
     *
     * @return the key
     */
    Key getKey();
}
