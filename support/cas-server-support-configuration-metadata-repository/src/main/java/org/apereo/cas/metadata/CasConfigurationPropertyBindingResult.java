package org.apereo.cas.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import jakarta.annotation.Nonnull;

/**
 * This is {@link CasConfigurationPropertyBindingResult}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record CasConfigurationPropertyBindingResult(ConfigurationProperty property, BindingStatus status) {

    @Nonnull
    @Override
    public String toString() {
        return String.format("\t%s = %s (Origin: %s) (Status: %s)%n",
            property.getName(), property.getValue(), property.getOrigin(), status);
    }

    @RequiredArgsConstructor
    @Getter
    public enum BindingStatus {
        UNKNOWN("Unknown"),
        DEPRECATED("Deprecated");

        private final String label;
    }
}
