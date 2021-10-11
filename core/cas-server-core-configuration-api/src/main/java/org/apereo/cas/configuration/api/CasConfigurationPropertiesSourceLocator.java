package org.apereo.cas.configuration.api;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;

import java.util.Optional;

/**
 * This is {@link CasConfigurationPropertiesSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface CasConfigurationPropertiesSourceLocator {

    /**
     * Standalone configuration profile.
     */
    String PROFILE_STANDALONE = "standalone";

    /**
     * Locate property sources for CAS via the given environment and other resources.
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return the property source
     */
    Optional<PropertySource<?>> locate(Environment environment, ResourceLoader resourceLoader);
}
