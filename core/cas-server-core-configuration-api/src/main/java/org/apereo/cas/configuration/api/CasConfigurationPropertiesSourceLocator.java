package org.apereo.cas.configuration.api;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;

/**
 * This is {@link CasConfigurationPropertiesSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface CasConfigurationPropertiesSourceLocator {

    /**
     * Locate property sources for CAS via the given environment and other resources.
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return the property source
     */
    PropertySource<?> locate(Environment environment, ResourceLoader resourceLoader);
}
