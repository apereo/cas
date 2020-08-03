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
     * The name of the customer header used to hold the identifier for the registered service used by the cipher..
     */
    String CUSTOM_HEADER_REGISTERED_SERVICE_ID = RegisteredService.class.getName();

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
    default String encode(final String data) {
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
     * @return true/false
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Supports boolean.
     *
     * @param registeredService the registered service
     * @return true/false
     */
    default boolean supports(final RegisteredService registeredService) {
        return true;
    }

    /**
     * Factory method.
     *
     * @return the registered service cipher executor
     */
    static RegisteredServiceCipherExecutor noOp() {
        return new RegisteredServiceCipherExecutor() {
            @Override
            public String encode(final String data, final Optional<RegisteredService> service) {
                return data;
            }

            @Override
            public String decode(final String data, final Optional<RegisteredService> service) {
                return data;
            }

            @Override
            public boolean supports(final RegisteredService registeredService) {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }
}
