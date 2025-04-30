package org.apereo.cas.configuration.support;

import java.util.Map;

/**
 * This is {@link ConfigurationPropertiesBindingContext}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record ConfigurationPropertiesBindingContext<T>(T value, Map<String, ConfigurationPropertyBindingResult> bindings) {
    /**
     * Contains binding for class.
     *
     * @param clazz the clazz
     * @return true/false
     */
    public boolean containsBindingFor(final Class clazz) {
        return bindings.values().stream().anyMatch(type -> type.containsBindingFor(clazz));
    }

    /**
     * Is bound successfully.
     *
     * @return true/false
     */
    public boolean isBound() {
        return value != null && !bindings.isEmpty();
    }
}
