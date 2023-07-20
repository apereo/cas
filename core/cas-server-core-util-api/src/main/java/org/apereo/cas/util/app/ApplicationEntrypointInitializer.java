package org.apereo.cas.util.app;

/**
 * This is {@link ApplicationEntrypointInitializer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface ApplicationEntrypointInitializer {
    /**
     * Sets main arguments.
     *
     * @param mainArguments the main arguments
     */
    void initialize(String[] mainArguments);
}
