package org.apereo.cas.configuration.api;

import module java.base;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * This is {@link MutablePropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface MutablePropertySource<T> {
    /**
     * Set property.
     *
     * @param name  the name
     * @param value the value
     */
    @CanIgnoreReturnValue
    MutablePropertySource setProperty(String name, Object value);

    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets source.
     *
     * @return the source
     */
    T getSource();
}
