package org.apereo.cas.services;

/**
 * Defines how to encrypt data based on registered service's public key, etc.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@FunctionalInterface
public interface RegisteredServiceCipherExecutor {
    /**
     * Encode string.
     *
     * @param data the data
     * @param service the service
     * @return the encoded string or null
     */
    String encode(String data, RegisteredService service);
}
