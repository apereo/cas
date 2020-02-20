package org.apereo.cas.services;

/**
 * This is {@link ServicesManagerExecutionPlanConfigurer}.
 *
 * @author Travis Schmidt
 * @since 6.2.0
 */
@FunctionalInterface
public interface ServicesManagerExecutionPlanConfigurer {
    /**
     * Configure services manager.
     *
     * @return the services manager
     */
    ServicesManager configureServicesManager();
}
