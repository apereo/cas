package org.jasig.cas.util.services;

import org.jasig.cas.services.RegisteredService;

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
     * @param data the data
     * @param service the service
     * @return the encoded string or null
     */
    String encode(String data, RegisteredService service);
}
