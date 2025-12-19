package org.apereo.cas.metadata;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.source.ConfigurationProperty;

/**
 * This is {@link CasConfigurationPropertyBindingResult}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record CasConfigurationPropertyBindingResult(ConfigurationProperty property, BindingStatus status) {

    @NonNull
    @Override
    public String toString() {
        return String.format("\t%s = %s (Origin: %s) (Status: %s)%n",
            property.getName(), property.getValue(), property.getOrigin(), status);
    }

    @RequiredArgsConstructor
    @Getter
    public enum BindingStatus {
        /**
         * Unknown binding status.
         */
        UNKNOWN("Unknown"),
        /**
         * Deprecated binding status.
         */
        DEPRECATED("Deprecated");

        private final String label;
    }
}
