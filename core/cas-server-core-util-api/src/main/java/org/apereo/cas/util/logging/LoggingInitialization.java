package org.apereo.cas.util.logging;

/**
 * This is {@link LoggingInitialization}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface LoggingInitialization {
    /**
     * Sets main arguments.
     *
     * @param mainArguments the main arguments
     */
    void setMainArguments(String[] mainArguments);
}
