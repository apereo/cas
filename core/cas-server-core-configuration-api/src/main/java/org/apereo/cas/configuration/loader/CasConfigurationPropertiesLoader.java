package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.NamedObject;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface CasConfigurationPropertiesLoader extends NamedObject {

    /**
     * Load property source.
     *
     * @param resource                    the resource
     * @param environment                 the environment
     * @param name                        the name
     * @param configurationCipherExecutor the configuration cipher executor
     * @return the property source
     */
    PropertySource load(Resource resource,
                        Environment environment,
                        String name,
                        CipherExecutor<String, String> configurationCipherExecutor);

    /**
     * Supports this resource.
     *
     * @param resource the resource
     * @return true/false
     */
    boolean supports(Resource resource);
}
