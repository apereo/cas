package org.apereo.cas.services.query;

/**
 * This is {@link RegisteredServiceQueryIndex}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface RegisteredServiceQueryIndex<T> {

    /**
     * To unique query index.
     *
     * @return the index.
     */
    T getIndex();
}
