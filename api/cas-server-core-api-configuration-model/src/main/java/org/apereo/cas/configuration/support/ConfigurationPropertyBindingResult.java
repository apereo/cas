package org.apereo.cas.configuration.support;

import java.util.List;

/**
 * This is {@link ConfigurationPropertyBindingResult}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record ConfigurationPropertyBindingResult(String name, Object value, List<Class> sources) {
    /**
     * Contains binding for class.
     *
     * @param clazz the clazz
     * @return true/false
     */
    public boolean containsBindingFor(final Class clazz) {
        return sources().contains(clazz);
    }
}
