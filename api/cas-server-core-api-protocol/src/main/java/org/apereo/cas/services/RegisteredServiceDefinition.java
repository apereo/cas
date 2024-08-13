package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;

/**
 * This is {@link RegisteredServiceDefinition}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RegisteredServiceDefinition extends Serializable {
    /**
     * Initial ID value of newly created (but not persisted) registered service.
     */
    long INITIAL_IDENTIFIER_VALUE = -1;

    /**
     * The numeric identifier for this service. Implementations
     * are expected to initialize the id with the value of {@link #INITIAL_IDENTIFIER_VALUE}.
     *
     * @return the numeric identifier for this service.
     */
    long getId();

    /**
     * Sets the identifier for this service. Use {@link #INITIAL_IDENTIFIER_VALUE} to
     * indicate a branch new service definition.
     *
     * @param id the numeric identifier for the service.
     * @return the id
     */
    RegisteredServiceDefinition setId(long id);

    /**
     * Returns the name of the service.
     *
     * @return the name of the service.
     */
    String getName();

    /**
     * Returns the description of the service.
     *
     * @return the description of the service.
     */
    default String getDescription() {
        return StringUtils.EMPTY;
    }
}
