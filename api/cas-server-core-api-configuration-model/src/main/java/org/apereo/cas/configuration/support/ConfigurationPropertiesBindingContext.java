package org.apereo.cas.configuration.support;

import module java.base;

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
     * Contains binding for class.
     *
     * @param property the property
     * @return true /false
     */
    public boolean containsBindingFor(final String property) {
        return bindings.containsKey(property);
    }

    /**
     * Whether any bound property is the given property or sits under it, such as
     * {@code cas.tgc.crypto.encryption.key} under {@code cas.tgc.crypto}.
     *
     * @param prefix the property prefix
     * @return true/false
     */
    public boolean containsBindingUnder(final String prefix) {
        return bindings.keySet().stream().anyMatch(name -> name.equals(prefix) || name.startsWith(prefix + '.'));
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
