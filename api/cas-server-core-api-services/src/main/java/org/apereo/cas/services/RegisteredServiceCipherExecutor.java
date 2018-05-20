package org.apereo.cas.services;

import java.util.Optional;

/**
 * Defines how to encrypt data based on registered service's public key, etc.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface RegisteredServiceCipherExecutor {
    /**
     * Encode string.
     *
     * @param data    the data
     * @param service the service
     * @return the encoded string or null
     */
    String encode(String data, Optional<RegisteredService> service);

    /**
     * Encode string.
     *
     * @param data the data
     * @return the string
     */
    default String encode(String data) {
        return encode(data, Optional.empty());
    }

    /**
     * Decode string.
     *
     * @param data    the data
     * @param service the service
     * @return the string
     */
    String decode(String data, Optional<RegisteredService> service);

    /**
     * Is enabled?.
     *
     * @return the boolean
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Supports boolean.
     *
     * @param registeredService the registered service
     * @return the boolean
     */
    default boolean supports(final RegisteredService registeredService) {
        return true;
    }
}
