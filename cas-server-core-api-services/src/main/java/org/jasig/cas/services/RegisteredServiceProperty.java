package org.jasig.cas.services;

import java.io.Serializable;
import java.util.Set;

/**
 * The {@link RegisteredServiceProperty} defines a single custom
 * property that is associated with a service.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface RegisteredServiceProperty extends Serializable {

    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets values.
     *
     * @return the values
     */
    Set<String> getValues();
}
