package org.apereo.cas.configuration.api;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link MutableConfigurationProperty}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record MutableConfigurationProperty(String name, @Nullable Object value, String propertySource) {
}
