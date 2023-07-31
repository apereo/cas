package org.apereo.cas.util.app;

import java.util.List;

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
    default void initialize(final String[] mainArguments) {
    }

    default List<Class> getApplicationSources() {
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
