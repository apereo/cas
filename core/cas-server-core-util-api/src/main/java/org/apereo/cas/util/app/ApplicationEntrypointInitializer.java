package org.apereo.cas.util.app;

import module java.base;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * This is {@link ApplicationEntrypointInitializer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface ApplicationEntrypointInitializer {
    /**
     * Sets main arguments.
     *
     * @param mainArguments the main arguments
     */
    @CanIgnoreReturnValue
    default ApplicationEntrypointInitializer initialize(final String[] mainArguments) {
        return this;
    }

    /**
     * Gets application sources.
     *
     * @param args the args
     * @return the application sources
     */
    default List<Class> getApplicationSources(final String[] args) {
        return List.of();
    }

    /**
     * No op application entrypoint initializer.
     *
     * @return the application entrypoint initializer
     */
    static ApplicationEntrypointInitializer noOp() {
        return new ApplicationEntrypointInitializer() {};
    }
}
