package org.apereo.cas.services;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceEntityMapper}.
 *
 * @param <R> the type parameter
 * @param <M> the type parameter
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface RegisteredServiceEntityMapper<R extends RegisteredService, M extends Serializable> {

    /**
     * To registered service.
     *
     * @param object the object
     * @return the r
     */
    R toRegisteredService(M object);

    /**
     * From registered service.
     *
     * @param service the service
     * @return the m
     */
    M fromRegisteredService(R service);
}
